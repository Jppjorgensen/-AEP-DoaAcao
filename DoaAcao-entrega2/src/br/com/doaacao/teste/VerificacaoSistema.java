package br.com.doaacao.teste;

import br.com.doaacao.controller.DoacaoController;
import br.com.doaacao.controller.ResumoDoacoes;
import br.com.doaacao.dao.ConexaoFactory;
import br.com.doaacao.dao.DoacaoDAOSqlite;
import br.com.doaacao.model.Alimento;
import br.com.doaacao.model.Doacao;
import br.com.doaacao.model.Higiene;
import br.com.doaacao.model.ItemDoacao;
import br.com.doaacao.model.Situacao;
import br.com.doaacao.model.TipoItem;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.List;

/**
 * Verificações automáticas do sistema, rodando num banco temporário (o banco de verdade não é tocado).
 * Execute com: run.sh --test   (ou run.bat --test)
 */
public class VerificacaoSistema {

    private int aprovadas = 0;
    private int reprovadas = 0;

    public static void main(String[] args) throws Exception {
        new VerificacaoSistema().executar();
    }

    private void executar() throws Exception {
        Path pasta = Files.createTempDirectory("doaacao-teste");
        ConexaoFactory.preparar(pasta.resolve("teste.db"), Path.of("database", "schema.sql"));
        DoacaoController controller = new DoacaoController(new DoacaoDAOSqlite());
        LocalDate hoje = LocalDate.now();

        System.out.println("Verificando o DoaAcao em um banco temporario...\n");

        // CREATE
        Doacao doacao = controller.cadastrar("Mercado Central", "Lar Esperanca", List.of(
                new Alimento("Arroz", 5, "pacote", hoje.plusDays(3)),
                new Higiene("Sabonete", 10, "unidade")));
        verificar("Cadastro gera um id e comeca como RECEBIDA",
                doacao.getId() != null && doacao.getSituacao() == Situacao.RECEBIDA);

        // READ (em uma nova conexão: prova que ficou gravado no arquivo)
        Doacao lida = controller.buscar(doacao.getId());
        verificar("Consulta traz os 2 itens gravados", lida.getItens().size() == 2);
        verificar("Itens voltam do banco com o tipo certo (Alimento e Higiene)",
                lida.getItens().get(0) instanceof Alimento && lida.getItens().get(1) instanceof Higiene);

        // Polimorfismo: o mesmo método resumo() responde diferente para cada tipo
        ItemDoacao alimento = lida.getItens().get(0);
        ItemDoacao higiene = lida.getItens().get(1);
        verificar("resumo() do Alimento fala da validade", alimento.resumo().contains("vence em 3 dias"));
        verificar("resumo() da Higiene fala da embalagem", higiene.resumo().contains("embalagem"));

        // Alerta de validade
        ResumoDoacoes resumo = controller.resumir(controller.listar(), hoje);
        verificar("Alerta conta o arroz que vence em 3 dias", resumo.getAlimentosVencendo() == 1);

        // UPDATE
        controller.atualizar(doacao.getId(), "Mercado Central Ltda", "Lar Esperanca", Situacao.RECEBIDA,
                List.of(new Higiene("Shampoo", 4, "frasco")));
        Doacao atualizada = controller.buscar(doacao.getId());
        verificar("Atualizacao troca o doador e os itens",
                atualizada.getDoador().equals("Mercado Central Ltda") && atualizada.getItens().size() == 1);

        Doacao entregue = controller.marcarComoEntregue(doacao.getId());
        verificar("Marcar como entregue muda a situacao", entregue.getSituacao() == Situacao.ENTREGUE
                && controller.buscar(doacao.getId()).getSituacao() == Situacao.ENTREGUE);

        // DELETE em cascata
        controller.excluir(doacao.getId());
        verificar("Exclusao remove a doacao", controller.listar().isEmpty());
        verificar("Exclusao remove os itens junto (cascata)", contarItensNoBanco() == 0);

        // Entradas inválidas são recusadas
        verificar("Recusa doacao sem itens", recusa(() -> controller.cadastrar("Ana", "Lar", List.of())));
        verificar("Recusa doador em branco", recusa(() -> controller.cadastrar("  ", "Lar",
                List.of(new Higiene("Sabonete", 1, "unidade")))));
        verificar("Recusa quantidade zero", recusa(() -> controller.criarItem(TipoItem.HIGIENE, "Sabonete", "0", "unidade", "")));
        verificar("Recusa quantidade que nao e numero", recusa(() -> controller.criarItem(TipoItem.HIGIENE, "Sabonete", "dez", "unidade", "")));
        verificar("Recusa alimento sem validade", recusa(() -> controller.criarItem(TipoItem.ALIMENTO, "Arroz", "1", "pacote", "")));
        verificar("Recusa data impossivel (31/02)", recusa(() -> controller.criarItem(TipoItem.ALIMENTO, "Arroz", "1", "pacote", "31/02/2027")));
        verificar("Aceita data no formato brasileiro", controller.criarItem(TipoItem.ALIMENTO, "Arroz", "1", "pacote", "25/12/2027")
                .getValidade().orElseThrow().equals(LocalDate.of(2027, 12, 25)));

        System.out.println("\nResultado: " + aprovadas + " aprovadas, " + reprovadas + " reprovadas.");
        if (reprovadas > 0) {
            System.exit(1);
        }
    }

    private void verificar(String descricao, boolean passou) {
        if (passou) {
            aprovadas++;
            System.out.println("  [OK]    " + descricao);
        } else {
            reprovadas++;
            System.out.println("  [FALHA] " + descricao);
        }
    }

    private boolean recusa(Runnable acao) {
        try {
            acao.run();
            return false;
        } catch (IllegalArgumentException e) {
            return true;
        }
    }

    private int contarItensNoBanco() throws Exception {
        try (Connection conn = ConexaoFactory.obterConexao();
             PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) AS total FROM itens_doacao");
             ResultSet rs = stmt.executeQuery()) {
            rs.next();
            return rs.getInt("total");
        }
    }
}
