package br.com.doaacao.view;

import br.com.doaacao.model.Doacao;

import javax.swing.table.AbstractTableModel;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Adapta a lista de doações para a JTable da tela principal (RF03). */
class DoacoesTableModel extends AbstractTableModel {

    private static final String[] COLUNAS = {"Nº", "Doador ou grupo", "Instituição", "Itens", "Situação"};

    private List<Doacao> doacoes = new ArrayList<>();

    void setDoacoes(List<Doacao> doacoes) {
        this.doacoes = new ArrayList<>(doacoes);
        fireTableDataChanged();
    }

    Doacao getDoacao(int linhaDoModelo) {
        return doacoes.get(linhaDoModelo);
    }

    boolean precisaDeAtencao(int linhaDoModelo) {
        return !doacoes.get(linhaDoModelo).itensQuePrecisamDeAtencao(LocalDate.now()).isEmpty();
    }

    @Override
    public int getRowCount() {
        return doacoes.size();
    }

    @Override
    public int getColumnCount() {
        return COLUNAS.length;
    }

    @Override
    public String getColumnName(int coluna) {
        return COLUNAS[coluna];
    }

    @Override
    public Class<?> getColumnClass(int coluna) {
        return coluna == 0 || coluna == 3 ? Long.class : String.class;
    }

    @Override
    public Object getValueAt(int linha, int coluna) {
        Doacao doacao = doacoes.get(linha);
        switch (coluna) {
            case 0:
                return doacao.getId();
            case 1:
                return doacao.getDoador();
            case 2:
                return doacao.getDestino();
            case 3:
                return (long) doacao.getItens().size();
            default:
                return doacao.getSituacao().getRotulo();
        }
    }
}
