package br.com.doaacao.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

/**
 * Cria o item certo (Alimento ou Higiene) a partir do que o voluntário digitou na tela.
 * Assim a interface não precisa conhecer as subclasses: ela só pede um ItemDoacao.
 */
public final class ItemDoacaoFactory {

    private static final DateTimeFormatter DATA_BR = DateTimeFormatter.ofPattern("dd/MM/uuuu")
            .withResolverStyle(ResolverStyle.STRICT);

    private ItemDoacaoFactory() {
    }

    public static ItemDoacao criar(TipoItem tipo, String descricao, String quantidadeTexto,
                                   String unidade, String validadeTexto) {
        if (tipo == null) {
            throw new IllegalArgumentException("Escolha o tipo do item.");
        }
        int quantidade = lerQuantidade(quantidadeTexto);
        switch (tipo) {
            case ALIMENTO:
                return new Alimento(descricao, quantidade, unidade, lerData(validadeTexto));
            case HIGIENE:
                return new Higiene(descricao, quantidade, unidade);
            default:
                throw new IllegalArgumentException("Tipo de item não suportado: " + tipo);
        }
    }

    /** Usado ao ler do banco, onde a validade está no formato ISO (AAAA-MM-DD). */
    public static ItemDoacao doBanco(String tipo, String descricao, int quantidade, String unidade, String validadeIso) {
        if (TipoItem.valueOf(tipo) == TipoItem.ALIMENTO) {
            return new Alimento(descricao, quantidade, unidade, LocalDate.parse(validadeIso));
        }
        return new Higiene(descricao, quantidade, unidade);
    }

    private static int lerQuantidade(String texto) {
        try {
            return Integer.parseInt(texto.trim());
        } catch (NumberFormatException | NullPointerException e) {
            throw new IllegalArgumentException("A quantidade precisa ser um número inteiro, como 3 ou 10.");
        }
    }

    private static LocalDate lerData(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException("Alimentos precisam ter a data de validade (dd/mm/aaaa).");
        }
        try {
            return LocalDate.parse(texto.trim(), DATA_BR);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Não reconheci a data \"" + texto.trim() + "\". Use dd/mm/aaaa, como 25/12/2026.");
        }
    }
}
