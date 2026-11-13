package br.com.doaacao.model;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Item de uma doação. É abstrata porque todo item é, na prática, um alimento ou um produto de higiene:
 * os dados em comum ficam aqui e cada tipo completa o que é dele (validade, resumo, cuidados).
 */
public abstract class ItemDoacao {

    private static final int LIMITE_TEXTO = 150;

    private final String descricao;
    private final int quantidade;
    private final String unidade;

    protected ItemDoacao(String descricao, int quantidade, String unidade) {
        this.descricao = textoObrigatorio(descricao, "Descreva o item");
        this.unidade = textoObrigatorio(unidade, "Informe a unidade (ex.: pacote, caixa, unidade)");
        if (quantidade <= 0) {
            throw new IllegalArgumentException("A quantidade precisa ser maior que zero.");
        }
        this.quantidade = quantidade;
    }

    public abstract TipoItem getTipo();

    /** Frase curta que aparece na tela de detalhes. Cada tipo de item escreve a sua. */
    public abstract String resumo();

    /** Só alimentos têm validade; para os outros itens o Optional vem vazio. */
    public Optional<LocalDate> getValidade() {
        return Optional.empty();
    }

    /** Se o item pede prioridade na entrega. Por padrão não; o Alimento sobrescreve. */
    public boolean precisaDeAtencao(LocalDate hoje) {
        return false;
    }

    public String getDescricao() {
        return descricao;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public String getUnidade() {
        return unidade;
    }

    protected String quantidadeComUnidade() {
        return quantidade + " " + unidade;
    }

    static String textoObrigatorio(String valor, String mensagem) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(mensagem + ".");
        }
        String limpo = valor.trim();
        if (limpo.length() > LIMITE_TEXTO) {
            throw new IllegalArgumentException("Use no máximo " + LIMITE_TEXTO + " caracteres.");
        }
        return limpo;
    }
}
