package br.com.doaacao.model;

/** Tipos de item aceitos. O nome da constante é o valor gravado na coluna "tipo" do banco. */
public enum TipoItem {
    ALIMENTO("Alimento"),
    HIGIENE("Higiene");

    private final String rotulo;

    TipoItem(String rotulo) {
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
