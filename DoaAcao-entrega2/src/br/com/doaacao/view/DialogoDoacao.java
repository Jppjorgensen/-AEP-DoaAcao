package br.com.doaacao.view;

import br.com.doaacao.controller.DoacaoController;
import br.com.doaacao.dao.PersistenciaException;
import br.com.doaacao.model.Doacao;
import br.com.doaacao.model.ItemDoacao;
import br.com.doaacao.model.Situacao;
import br.com.doaacao.model.TipoItem;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

/**
 * Janela para cadastrar uma doação nova ou editar uma existente.
 * Os itens são montados aqui e só vão para o banco quando a pessoa clica em Salvar.
 */
class DialogoDoacao {

    private final DoacaoController controller;
    private final Doacao original;
    private final JDialog dialogo;

    private final JTextField txtDoador = Tema.campo();
    private final JTextField txtDestino = Tema.campo();
    private final JComboBox<Situacao> cbSituacao = new JComboBox<>(Situacao.values());

    private final DefaultListModel<ItemDoacao> modeloItens = new DefaultListModel<>();
    private final JList<ItemDoacao> listaItens = new JList<>(modeloItens);

    private final JComboBox<TipoItem> cbTipo = new JComboBox<>(TipoItem.values());
    private final JTextField txtDescricao = Tema.campo();
    private final JTextField txtQuantidade = Tema.campo();
    private final JTextField txtUnidade = Tema.campo();
    private final JTextField txtValidade = Tema.campo();
    private final JLabel lblErro = new JLabel(" ");

    private Doacao salva;

    /** @param original a doação a editar, ou null para cadastrar uma nova */
    DialogoDoacao(JFrame dono, DoacaoController controller, Doacao original) {
        this.controller = controller;
        this.original = original;
        this.dialogo = new JDialog(dono, original == null ? "Nova doação" : "Doação nº " + original.getId(), true);
        montar();
        if (original != null) {
            txtDoador.setText(original.getDoador());
            txtDestino.setText(original.getDestino());
            cbSituacao.setSelectedItem(original.getSituacao());
            for (ItemDoacao item : original.getItens()) {
                modeloItens.addElement(item);
            }
        }
    }

    /** Mostra a janela e devolve a doação salva, ou null se a pessoa cancelou. */
    Doacao abrir() {
        dialogo.setVisible(true);
        return salva;
    }

    private void montar() {
        JPanel conteudo = new JPanel(new BorderLayout(0, 14));
        conteudo.setBackground(Tema.FUNDO);
        conteudo.setBorder(BorderFactory.createEmptyBorder(18, 20, 16, 20));
        conteudo.add(criarDadosGerais(), BorderLayout.NORTH);
        conteudo.add(criarItens(), BorderLayout.CENTER);
        conteudo.add(criarRodape(), BorderLayout.SOUTH);

        dialogo.setContentPane(conteudo);
        dialogo.setSize(720, 720);
        dialogo.setMinimumSize(new Dimension(640, 560));
        dialogo.setLocationRelativeTo(dialogo.getOwner());
        dialogo.getRootPane().setDefaultButton(null);
    }

    private JPanel criarDadosGerais() {
        JPanel painel = cartao(new GridBagLayout());
        GridBagConstraints c = restricoes();
        adicionarCampo(painel, c, 0, "Quem está doando? (pessoa, empresa ou grupo)", txtDoador);
        adicionarCampo(painel, c, 1, "Para qual instituição vai?", txtDestino);
        if (original != null) {
            cbSituacao.setFont(Tema.fonte(Font.PLAIN, 14));
            adicionarCampo(painel, c, 2, "Situação", cbSituacao);
        }
        return painel;
    }

    private JPanel criarItens() {
        JLabel titulo = new JLabel("Itens da doação");
        titulo.setFont(Tema.fonte(Font.BOLD, 15));
        titulo.setForeground(Tema.TEXTO);

        listaItens.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listaItens.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> lista, Object valor, int indice,
                                                          boolean selecionado, boolean foco) {
                ItemDoacao item = (ItemDoacao) valor;
                super.getListCellRendererComponent(lista, item.resumo(), indice, selecionado, false);
                setText("<html><div style='width:440px'><b>" + item.getTipo().getRotulo() + ":</b> "
                        + item.resumo() + "</div></html>");
                setFont(Tema.fonte(Font.PLAIN, 14));
                setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
                if (selecionado) {
                    setBackground(Tema.SELECAO);
                    setForeground(Tema.TEXTO);
                }
                return this;
            }
        });
        JScrollPane rolagem = new JScrollPane(listaItens);
        rolagem.setBorder(BorderFactory.createLineBorder(Tema.BORDA));
        rolagem.setPreferredSize(new Dimension(400, 130));
        rolagem.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        Botao btnRemover = Botao.perigo("Remover item selecionado");
        btnRemover.addActionListener(e -> {
            int indice = listaItens.getSelectedIndex();
            if (indice >= 0) {
                modeloItens.remove(indice);
            }
        });
        JPanel linhaRemover = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        linhaRemover.setOpaque(false);
        linhaRemover.add(btnRemover);

        JPanel lista = new JPanel(new BorderLayout(0, 8));
        lista.setOpaque(false);
        lista.add(titulo, BorderLayout.NORTH);
        lista.add(rolagem, BorderLayout.CENTER);
        lista.add(linhaRemover, BorderLayout.SOUTH);

        JPanel itens = new JPanel(new BorderLayout(0, 12));
        itens.setOpaque(false);
        itens.add(lista, BorderLayout.CENTER);
        itens.add(criarNovoItem(), BorderLayout.SOUTH);
        return itens;
    }

    private JPanel criarNovoItem() {
        JPanel painel = cartao(new GridBagLayout());
        GridBagConstraints c = restricoes();

        cbTipo.setFont(Tema.fonte(Font.PLAIN, 14));
        cbTipo.addActionListener(e -> ajustarCampoValidade());
        txtValidade.setToolTipText("Formato dd/mm/aaaa, por exemplo 25/12/2026");
        txtQuantidade.setColumns(5);

        JPanel linha1 = linha();
        linha1.add(rotulado("Tipo", cbTipo), proporcao(0, 0.25));
        linha1.add(rotulado("O que é? (ex.: Arroz 5 kg)", txtDescricao), proporcao(1, 0.75));

        JPanel linha2 = linha();
        linha2.add(rotulado("Quantidade", txtQuantidade), proporcao(0, 0.25));
        linha2.add(rotulado("Unidade (pacote, caixa, kg...)", txtUnidade), proporcao(1, 0.4));
        linha2.add(rotulado("Validade (dd/mm/aaaa)", txtValidade), proporcao(2, 0.35));

        Botao btnAdicionar = Botao.secundario("+ Adicionar item");
        btnAdicionar.addActionListener(e -> adicionarItem());
        txtValidade.addActionListener(e -> adicionarItem());
        JPanel linha3 = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        linha3.setOpaque(false);
        linha3.add(btnAdicionar);

        c.gridy = 0;
        painel.add(linha1, c);
        c.gridy = 1;
        painel.add(linha2, c);
        c.gridy = 2;
        painel.add(linha3, c);
        ajustarCampoValidade();
        return painel;
    }

    private JPanel criarRodape() {
        lblErro.setFont(Tema.fonte(Font.BOLD, 13));
        lblErro.setForeground(Tema.VERMELHO);

        Botao btnCancelar = Botao.secundario("Cancelar");
        Botao btnSalvar = Botao.principal(original == null ? "Registrar doação" : "Salvar alterações");
        btnCancelar.addActionListener(e -> dialogo.dispose());
        btnSalvar.addActionListener(e -> salvar());
        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botoes.setOpaque(false);
        botoes.add(btnCancelar);
        botoes.add(btnSalvar);

        JPanel rodape = new JPanel(new BorderLayout(10, 0));
        rodape.setOpaque(false);
        rodape.add(lblErro, BorderLayout.CENTER);
        rodape.add(botoes, BorderLayout.EAST);
        return rodape;
    }

    // ------------------------------------------------------------------ ações

    /** A validade só faz sentido para alimentos, então o campo fica desligado para higiene. */
    private void ajustarCampoValidade() {
        boolean alimento = cbTipo.getSelectedItem() == TipoItem.ALIMENTO;
        txtValidade.setEnabled(alimento);
        txtValidade.setBackground(alimento ? Tema.CARTAO : Tema.LINHA_ALTERNADA);
        if (!alimento) {
            txtValidade.setText("");
        }
    }

    private void adicionarItem() {
        try {
            ItemDoacao item = controller.criarItem((TipoItem) cbTipo.getSelectedItem(), txtDescricao.getText(),
                    txtQuantidade.getText(), txtUnidade.getText(), txtValidade.getText());
            modeloItens.addElement(item);
            txtDescricao.setText("");
            txtQuantidade.setText("");
            txtUnidade.setText("");
            txtValidade.setText("");
            lblErro.setText(" ");
            txtDescricao.requestFocusInWindow();
        } catch (IllegalArgumentException e) {
            lblErro.setText(e.getMessage());
        }
    }

    private void salvar() {
        List<ItemDoacao> itens = new ArrayList<>();
        for (int i = 0; i < modeloItens.size(); i++) {
            itens.add(modeloItens.get(i));
        }
        try {
            if (original == null) {
                salva = controller.cadastrar(txtDoador.getText(), txtDestino.getText(), itens);
            } else {
                salva = controller.atualizar(original.getId(), txtDoador.getText(), txtDestino.getText(),
                        (Situacao) cbSituacao.getSelectedItem(), itens);
            }
            dialogo.dispose();
        } catch (IllegalArgumentException e) {
            lblErro.setText(e.getMessage());
        } catch (PersistenciaException e) {
            lblErro.setText("Não foi possível salvar: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------ ajudantes de layout

    private static JPanel cartao(java.awt.LayoutManager layout) {
        JPanel painel = new JPanel(layout);
        painel.setBackground(Tema.CARTAO);
        painel.setBorder(Tema.bordaCartao());
        return painel;
    }

    private static GridBagConstraints restricoes() {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(4, 0, 4, 0);
        return c;
    }

    private static void adicionarCampo(JPanel painel, GridBagConstraints c, int linha, String rotulo, JComponent campo) {
        c.gridy = linha;
        painel.add(rotulado(rotulo, campo), c);
    }

    private static JPanel rotulado(String rotulo, JComponent campo) {
        JLabel lbl = new JLabel(rotulo);
        lbl.setFont(Tema.fonte(Font.PLAIN, 13));
        lbl.setForeground(Tema.TEXTO_SUAVE);
        JPanel painel = new JPanel(new BorderLayout(0, 3));
        painel.setOpaque(false);
        painel.add(lbl, BorderLayout.NORTH);
        painel.add(campo, BorderLayout.CENTER);
        return painel;
    }

    private static JPanel linha() {
        JPanel painel = new JPanel(new GridBagLayout());
        painel.setOpaque(false);
        return painel;
    }

    private static GridBagConstraints proporcao(int coluna, double peso) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = coluna;
        c.weightx = peso;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, coluna == 0 ? 0 : 10, 0, 0);
        return c;
    }
}
