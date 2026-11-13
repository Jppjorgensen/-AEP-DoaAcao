package br.com.doaacao.model;

/** Produto de higiene doado (sabonete, creme dental, absorvente...). Não tem validade controlada. */
public class Higiene extends ItemDoacao {

    public Higiene(String descricao, int quantidade, String unidade) {
        super(descricao, quantidade, unidade);
    }

    @Override
    public TipoItem getTipo() {
        return TipoItem.HIGIENE;
    }

    @Override
    public String resumo() {
        return getDescricao() + " (" + quantidadeComUnidade() + ") - conferir se a embalagem está lacrada";
    }
}
