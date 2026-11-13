package br.com.doaacao.model;

/** Em que pé está a doação: ainda com o grupo ou já entregue à instituição. */
public enum Situacao {
    RECEBIDA("Aguardando"),
    ENTREGUE("Entregue");

    private final String rotulo;

    Situacao(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }

    @Override
    public String toString() {
        return rotulo;
    }
}
