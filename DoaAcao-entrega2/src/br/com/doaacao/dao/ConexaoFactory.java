package br.com.doaacao.dao;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Único lugar que sabe onde fica o banco. Toda conexão já sai com as chaves estrangeiras
 * ligadas (no SQLite elas vêm desligadas por padrão), senão o ON DELETE CASCADE não funciona.
 */
public final class ConexaoFactory {

    private static String url;

    private ConexaoFactory() {
    }

    /**
     * Aponta para o arquivo do banco e cria as tabelas a partir do script, se ainda não existirem.
     * Chamado uma vez quando o programa abre (e pelos testes, com um banco temporário).
     */
    public static void preparar(Path arquivoBanco, Path scriptSql) {
        try {
            Path pasta = arquivoBanco.toAbsolutePath().getParent();
            if (pasta != null) {
                Files.createDirectories(pasta);
            }
            url = "jdbc:sqlite:" + arquivoBanco.toAbsolutePath();
            String script = Files.readString(scriptSql);
            try (Connection conn = obterConexao(); Statement stmt = conn.createStatement()) {
                for (String comando : script.split(";")) {
                    if (!comando.isBlank()) {
                        stmt.execute(comando);
                    }
                }
            }
        } catch (IOException | SQLException e) {
            throw new PersistenciaException("Não foi possível preparar o banco de dados: " + e.getMessage(), e);
        }
    }

    public static Connection obterConexao() throws SQLException {
        if (url == null) {
            throw new IllegalStateException("Chame ConexaoFactory.preparar(...) antes de usar o banco.");
        }
        Connection conn = DriverManager.getConnection(url);
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON");
        }
        return conn;
    }
}
