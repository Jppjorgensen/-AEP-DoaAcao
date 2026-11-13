package br.com.doaacao.controller;

/** Números do painel de resumo (RF07) e do alerta de validade. */
public class ResumoDoacoes {

    private final int total;
    private final int recebidas;
    private final int entregues;
    private final int alimentosVencendo;

    public ResumoDoacoes(int total, int recebidas, int entregues, int alimentosVencendo) {
        this.total = total;
        this.recebidas = recebidas;
        this.entregues = entregues;
        this.alimentosVencendo = alimentosVencendo;
    }

    public int getTotal() {
        return total;
    }

    public int getRecebidas() {
        return recebidas;
    }

    public int getEntregues() {
        return entregues;
    }

    public int getAlimentosVencendo() {
        return alimentosVencendo;
    }
}
