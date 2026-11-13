package br.com.doaacao.controller;

import br.com.doaacao.dao.DoacaoDAO;
import br.com.doaacao.model.Alimento;
import br.com.doaacao.model.Doacao;
import br.com.doaacao.model.Higiene;
import br.com.doaacao.model.ItemDoacao;
import br.com.doaacao.model.ItemDoacaoFactory;
import br.com.doaacao.model.Situacao;
import br.com.doaacao.model.TipoItem;

import java.time.LocalDate;
import java.util.List;

/**
 * Faz a ponte entre as telas e o banco: recebe o que o voluntário digitou, monta os objetos
 * do domínio (que validam os dados) e pede ao DAO para gravar. Não conhece nada de Swing.
 */
public class DoacaoController {

    private final DoacaoDAO dao;

    public DoacaoController(DoacaoDAO dao) {
        this.dao = dao;
    }

    public List<Doacao> listar() {
        return dao.listarTodas();
    }

    public Doacao buscar(long id) {
        return dao.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Essa doação não existe mais. Atualize a lista."));
    }

    /** RF01 + RF02: nova doação, sempre como RECEBIDA. */
    public Doacao cadastrar(String doador, String destino, List<ItemDoacao> itens) {
        Doacao doacao = new Doacao(doador, destino, itens);
        dao.salvar(doacao);
        return doacao;
    }

    /** RF04: altera doador, instituição, situação e itens. */
    public Doacao atualizar(long id, String doador, String destino, Situacao situacao, List<ItemDoacao> itens) {
        Doacao doacao = new Doacao(id, doador, destino, situacao, itens);
        if (!dao.atualizar(doacao)) {
            throw new IllegalArgumentException("Essa doação não existe mais. Atualize a lista.");
        }
        return doacao;
    }

    /** RF06: atalho para o caso mais comum do dia a dia, que é registrar a entrega. */
    public Doacao marcarComoEntregue(long id) {
        Doacao doacao = buscar(id);
        doacao.marcarComoEntregue();
        dao.atualizar(doacao);
        return doacao;
    }

    /** RF05: a confirmação acontece na tela; aqui a exclusão é feita de fato. */
    public void excluir(long id) {
        if (!dao.excluir(id)) {
            throw new IllegalArgumentException("Essa doação não existe mais. Atualize a lista.");
        }
    }

    public ItemDoacao criarItem(TipoItem tipo, String descricao, String quantidade, String unidade, String validade) {
        return ItemDoacaoFactory.criar(tipo, descricao, quantidade, unidade, validade);
    }

    /** RF07 + alerta de validade: conta as doações por situação e os alimentos vencendo. */
    public ResumoDoacoes resumir(List<Doacao> doacoes, LocalDate hoje) {
        int recebidas = (int) doacoes.stream().filter(d -> d.getSituacao() == Situacao.RECEBIDA).count();
        int vencendo = doacoes.stream().mapToInt(d -> d.itensQuePrecisamDeAtencao(hoje).size()).sum();
        return new ResumoDoacoes(doacoes.size(), recebidas, doacoes.size() - recebidas, vencendo);
    }

    /** Preenche o banco com alguns exemplos, para quem abre o sistema pela primeira vez poder explorar. */
    public void carregarExemplos() {
        LocalDate hoje = LocalDate.now();
        cadastrar("Mercado Bom Preço", "Lar São Vicente", List.of(
                new Alimento("Arroz tipo 1", 10, "pacote 5 kg", hoje.plusMonths(8)),
                new Alimento("Leite integral", 24, "caixa 1 L", hoje.plusDays(4)),
                new Higiene("Sabonete", 30, "unidade")));
        cadastrar("Turma esoft4sm", "Casa da Criança Feliz", List.of(
                new Alimento("Feijão carioca", 15, "pacote 1 kg", hoje.plusMonths(5)),
                new Higiene("Creme dental", 20, "tubo")));
        Doacao entregue = cadastrar("Família Oliveira", "Lar São Vicente", List.of(
                new Alimento("Macarrão", 12, "pacote 500 g", hoje.plusMonths(10))));
        marcarComoEntregue(entregue.getId());
    }
}
