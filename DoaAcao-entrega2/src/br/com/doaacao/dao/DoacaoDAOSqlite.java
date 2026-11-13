package br.com.doaacao.dao;

import br.com.doaacao.model.Doacao;
import br.com.doaacao.model.ItemDoacao;
import br.com.doaacao.model.ItemDoacaoFactory;
import br.com.doaacao.model.Situacao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Implementação do DAO com SQLite + JDBC.
 * Doação e itens são gravados juntos numa transação: ou entra tudo, ou nada (rollback).
 */
public class DoacaoDAOSqlite implements DoacaoDAO {

    private static final String INSERIR_DOACAO =
            "INSERT INTO doacoes (doador, destino, status) VALUES (?, ?, ?)";
    private static final String ATUALIZAR_DOACAO =
            "UPDATE doacoes SET doador = ?, destino = ?, status = ? WHERE id = ?";
    private static final String APAGAR_ITENS = "DELETE FROM itens_doacao WHERE doacao_id = ?";
    private static final String INSERIR_ITEM =
            "INSERT INTO itens_doacao (doacao_id, tipo, descricao, quantidade, unidade, validade) VALUES (?, ?, ?, ?, ?, ?)";
    private static final String EXCLUIR_DOACAO = "DELETE FROM doacoes WHERE id = ?";
    private static final String BUSCAR_DOACAO = "SELECT id, doador, destino, status FROM doacoes WHERE id = ?";
    private static final String BUSCAR_ITENS_DA_DOACAO =
            "SELECT doacao_id, tipo, descricao, quantidade, unidade, validade FROM itens_doacao WHERE doacao_id = ? ORDER BY id";
    private static final String LISTAR_DOACOES = "SELECT id, doador, destino, status FROM doacoes ORDER BY id DESC";
    private static final String LISTAR_ITENS =
            "SELECT doacao_id, tipo, descricao, quantidade, unidade, validade FROM itens_doacao ORDER BY id";

    @Override
    public void salvar(Doacao doacao) {
        try (Connection conn = ConexaoFactory.obterConexao()) {
            conn.setAutoCommit(false);
            try {
                long id;
                try (PreparedStatement stmt = conn.prepareStatement(INSERIR_DOACAO, Statement.RETURN_GENERATED_KEYS)) {
                    preencherDoacao(stmt, doacao);
                    stmt.executeUpdate();
                    try (ResultSet chaves = stmt.getGeneratedKeys()) {
                        chaves.next();
                        id = chaves.getLong(1);
                    }
                }
                inserirItens(conn, id, doacao.getItens());
                conn.commit();
                doacao.setId(id);
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Não foi possível salvar a doação.", e);
        }
    }

    @Override
    public boolean atualizar(Doacao doacao) {
        try (Connection conn = ConexaoFactory.obterConexao()) {
            conn.setAutoCommit(false);
            try {
                int linhas;
                try (PreparedStatement stmt = conn.prepareStatement(ATUALIZAR_DOACAO)) {
                    preencherDoacao(stmt, doacao);
                    stmt.setLong(4, doacao.getId());
                    linhas = stmt.executeUpdate();
                }
                if (linhas == 0) {
                    conn.rollback();
                    return false;
                }
                // Os itens são regravados do zero: é mais simples e seguro do que comparar item por item.
                try (PreparedStatement stmt = conn.prepareStatement(APAGAR_ITENS)) {
                    stmt.setLong(1, doacao.getId());
                    stmt.executeUpdate();
                }
                inserirItens(conn, doacao.getId(), doacao.getItens());
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Não foi possível atualizar a doação.", e);
        }
    }

    @Override
    public boolean excluir(long id) {
        try (Connection conn = ConexaoFactory.obterConexao();
             PreparedStatement stmt = conn.prepareStatement(EXCLUIR_DOACAO)) {
            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new PersistenciaException("Não foi possível excluir a doação.", e);
        }
    }

    @Override
    public Optional<Doacao> buscarPorId(long id) {
        try (Connection conn = ConexaoFactory.obterConexao();
             PreparedStatement stmt = conn.prepareStatement(BUSCAR_DOACAO)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                List<ItemDoacao> itens = new ArrayList<>();
                try (PreparedStatement stmtItens = conn.prepareStatement(BUSCAR_ITENS_DA_DOACAO)) {
                    stmtItens.setLong(1, id);
                    try (ResultSet rsItens = stmtItens.executeQuery()) {
                        while (rsItens.next()) {
                            itens.add(mapearItem(rsItens));
                        }
                    }
                }
                return Optional.of(mapearDoacao(rs, itens));
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Não foi possível consultar a doação.", e);
        }
    }

    @Override
    public List<Doacao> listarTodas() {
        try (Connection conn = ConexaoFactory.obterConexao()) {
            // Busca todos os itens de uma vez e agrupa por doação (evita uma consulta por doação).
            Map<Long, List<ItemDoacao>> itensPorDoacao = new HashMap<>();
            try (PreparedStatement stmt = conn.prepareStatement(LISTAR_ITENS);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    itensPorDoacao.computeIfAbsent(rs.getLong("doacao_id"), chave -> new ArrayList<>())
                            .add(mapearItem(rs));
                }
            }
            List<Doacao> doacoes = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement(LISTAR_DOACOES);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    doacoes.add(mapearDoacao(rs, itensPorDoacao.get(rs.getLong("id"))));
                }
            }
            return doacoes;
        } catch (SQLException e) {
            throw new PersistenciaException("Não foi possível listar as doações.", e);
        }
    }

    private void preencherDoacao(PreparedStatement stmt, Doacao doacao) throws SQLException {
        stmt.setString(1, doacao.getDoador());
        stmt.setString(2, doacao.getDestino());
        stmt.setString(3, doacao.getSituacao().name());
    }

    private void inserirItens(Connection conn, long doacaoId, List<ItemDoacao> itens) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(INSERIR_ITEM)) {
            for (ItemDoacao item : itens) {
                stmt.setLong(1, doacaoId);
                stmt.setString(2, item.getTipo().name());
                stmt.setString(3, item.getDescricao());
                stmt.setInt(4, item.getQuantidade());
                stmt.setString(5, item.getUnidade());
                if (item.getValidade().isPresent()) {
                    stmt.setString(6, item.getValidade().get().toString());
                } else {
                    stmt.setNull(6, Types.VARCHAR);
                }
                stmt.executeUpdate();
            }
        }
    }

    private Doacao mapearDoacao(ResultSet rs, List<ItemDoacao> itens) throws SQLException {
        return new Doacao(rs.getLong("id"), rs.getString("doador"), rs.getString("destino"),
                Situacao.valueOf(rs.getString("status")), itens);
    }

    private ItemDoacao mapearItem(ResultSet rs) throws SQLException {
        return ItemDoacaoFactory.doBanco(rs.getString("tipo"), rs.getString("descricao"),
                rs.getInt("quantidade"), rs.getString("unidade"), rs.getString("validade"));
    }
}
