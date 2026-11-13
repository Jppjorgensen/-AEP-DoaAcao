package br.com.doaacao.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

/** Alimento doado. Tem validade, e é ela que decide se a entrega precisa ser priorizada. */
public class Alimento extends ItemDoacao {

    /** Alimentos que vencem dentro deste prazo aparecem no alerta da tela principal. */
    public static final int DIAS_DE_ALERTA = 7;
    private static final DateTimeFormatter DATA_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final LocalDate validade;

    public Alimento(String descricao, int quantidade, String unidade, LocalDate validade) {
        super(descricao, quantidade, unidade);
        if (validade == null) {
            throw new IllegalArgumentException("Alimentos precisam ter a data de validade.");
        }
        this.validade = validade;
    }

    @Override
    public TipoItem getTipo() {
        return TipoItem.ALIMENTO;
    }

    @Override
    public Optional<LocalDate> getValidade() {
        return Optional.of(validade);
    }

    @Override
    public boolean precisaDeAtencao(LocalDate hoje) {
        return !validade.isAfter(hoje.plusDays(DIAS_DE_ALERTA));
    }

    @Override
    public String resumo() {
        return getDescricao() + " (" + quantidadeComUnidade() + ") - " + situacaoDaValidade(LocalDate.now());
    }

    /** Texto amigável sobre a validade: "vence em 3 dias", "vence hoje", "venceu há 2 dias"... */
    public String situacaoDaValidade(LocalDate hoje) {
        long dias = ChronoUnit.DAYS.between(hoje, validade);
        if (dias < 0) {
            return "venceu em " + validade.format(DATA_BR) + ", confira antes de entregar";
        }
        if (dias == 0) {
            return "vence hoje";
        }
        if (dias == 1) {
            return "vence amanhã";
        }
        if (dias <= DIAS_DE_ALERTA) {
            return "vence em " + dias + " dias";
        }
        return "validade " + validade.format(DATA_BR);
    }
}
