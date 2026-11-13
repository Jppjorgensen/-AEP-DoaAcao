package br.com.doaacao.view;

import br.com.doaacao.controller.DoacaoController;
import br.com.doaacao.controller.ResumoDoacoes;
import br.com.doaacao.dao.PersistenciaException;
import br.com.doaacao.model.Alimento;
import br.com.doaacao.model.Doacao;
import br.com.doaacao.model.ItemDoacao;
import br.com.doaacao.model.Situacao;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Tela principal: resumo no topo, lista de doações com busca à esquerda e
 * os detalhes da doação selecionada à direita.
 * A tela só exibe e repassa ações: quem decide e grava é o DoacaoController.
 */
public class TelaPrincipal {

    private final DoacaoController controller;
    private final JFrame janela = new JFrame("DoaAção");

    private final CartaoResumo cartaoTotal = new CartaoResumo("doações registradas", Tema.TEXTO);
    private final CartaoResumo cartaoRecebidas = new CartaoResumo("aguardando entrega", Tema.AZUL);
    private final CartaoResumo cartaoEntregues = new CartaoResumo("já entregues", Tema.VERDE_CLARO);
    private final CartaoResumo cartaoVencendo = new CartaoResumo("alimentos vencendo em 7 dias", Tema.ALERTA_TEXTO);
    private final JLabel faixaAlerta = new JLabel();

    private final DoacoesTableModel modeloTabela = new DoacoesTableModel();
    private final JTable tabela = new JTable(modeloTabela);
    private final TableRowSorter<DoacoesTableModel> ordenador = new TableRowSorter<>(modeloTabela);
    private final JTextField txtBusca = Tema.campo();

    private final CardLayout cartoesDetalhe = new CardLayout();
    private final JPanel painelDetalhe = new JPanel(cartoesDetalhe);
    private final JLabel lblDoador = new JLabel();
    private final JLabel lblDestino = new JLabel();
    private final JLabel lblSituacao = new JLabel();
    private final DefaultListModel<ItemDoacao> modeloItens = new DefaultListModel<>();
    private final JList<ItemDoacao> listaItens = new JList<>(modeloItens);
    private final Botao btnEntregue = Botao.principal("Marcar como entregue");

    public TelaPrincipal(DoacaoController controller) {
        this.controller = controller;
    }

    public void abrir() {
        JPanel conteudo = new JPanel(new BorderLayout(0, 14));
        conteudo.setBackground(Tema.FUNDO);
        conteudo.add(criarCabecalho(), BorderLayout.NORTH);

        JPanel corpo = new JPanel(new BorderLayout(0, 14));
        corpo.setOpaque(false);
        corpo.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));
        corpo.add(criarResumo(), BorderLayout.NORTH);

        JPanel centro = new JPanel(new BorderLayout(14, 0));
        centro.setOpaque(false);
        centro.add(criarLista(), BorderLayout.CENTER);
        centro.add(criarDetalhe(), BorderLayout.EAST);
        corpo.add(centro, BorderLayout.CENTER);
        conteudo.add(corpo, BorderLayout.CENTER);

        janela.setContentPane(conteudo);
        janela.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        janela.setSize(1180, 760);
        janela.setMinimumSize(new Dimension(1000, 640));
        janela.setLocationRelativeTo(null);
        recarregar(null);
        janela.setVisible(true);
    }

    // ------------------------------------------------------------------ montagem da tela

    private JPanel criarCabecalho() {
        JLabel titulo = new JLabel("DoaAção");
        titulo.setFont(Tema.fonte(Font.BOLD, 28));
        titulo.setForeground(Color.WHITE);
        JLabel subtitulo = new JLabel("Organize as doações do grupo e garanta que os alimentos cheguem a quem precisa.");
        subtitulo.setFont(Tema.fonte(Font.PLAIN, 14));
        subtitulo.setForeground(new Color(220, 235, 226));

        JPanel textos = new JPanel(new GridLayout(2, 1));
        textos.setOpaque(false);
        textos.add(titulo);
        textos.add(subtitulo);

        JLabel selo = new JLabel("ODS 2 · Fome Zero", SwingConstants.CENTER);
        selo.setOpaque(true);
        selo.setBackground(Tema.MOSTARDA_ODS2);
        selo.setForeground(Color.WHITE);
        selo.setFont(Tema.fonte(Font.BOLD, 13));
        selo.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        JPanel caixaSelo = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 12));
        caixaSelo.setOpaque(false);
        caixaSelo.add(selo);

        JPanel cabecalho = new JPanel(new BorderLayout());
        cabecalho.setBackground(Tema.VERDE);
        cabecalho.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        cabecalho.add(textos, BorderLayout.CENTER);
        cabecalho.add(caixaSelo, BorderLayout.EAST);
        return cabecalho;
    }

    private JPanel criarResumo() {
        JPanel cartoes = new JPanel(new GridLayout(1, 4, 12, 0));
        cartoes.setOpaque(false);
        cartoes.add(cartaoTotal);
        cartoes.add(cartaoRecebidas);
        cartoes.add(cartaoEntregues);
        cartoes.add(cartaoVencendo);

        faixaAlerta.setOpaque(true);
        faixaAlerta.setBackground(Tema.ALERTA_FUNDO);
        faixaAlerta.setForeground(Tema.ALERTA_TEXTO);
        faixaAlerta.setFont(Tema.fonte(Font.BOLD, 14));
        faixaAlerta.setBorder(BorderFactory.createEmptyBorder(9, 14, 9, 14));

        JPanel resumo = new JPanel(new BorderLayout(0, 10));
        resumo.setOpaque(false);
        resumo.add(cartoes, BorderLayout.CENTER);
        resumo.add(faixaAlerta, BorderLayout.SOUTH);
        return resumo;
    }

    private JPanel criarLista() {
        JLabel lblBusca = new JLabel("Buscar por doador ou instituição:");
        lblBusca.setFont(Tema.fonte(Font.PLAIN, 13));
        lblBusca.setForeground(Tema.TEXTO_SUAVE);
        txtBusca.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filtrar();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filtrar();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filtrar();
            }
        });
        Botao btnNova = Botao.principal("+ Nova doação");
        btnNova.addActionListener(e -> novaDoacao());

        JPanel busca = new JPanel(new BorderLayout(10, 4));
        busca.setOpaque(false);
        busca.add(lblBusca, BorderLayout.NORTH);
        busca.add(txtBusca, BorderLayout.CENTER);
        busca.add(btnNova, BorderLayout.EAST);

        tabela.setRowSorter(ordenador);
        tabela.setRowHeight(34);
        tabela.setFont(Tema.fonte(Font.PLAIN, 14));
        tabela.getTableHeader().setFont(Tema.fonte(Font.BOLD, 13));
        tabela.getTableHeader().setReorderingAllowed(false);
        tabela.setShowVerticalLines(false);
        tabela.setGridColor(Tema.BORDA);
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.setDefaultRenderer(Object.class, new RenderizadorLinha());
        tabela.setDefaultRenderer(Long.class, new RenderizadorLinha());
        tabela.getColumnModel().getColumn(0).setMaxWidth(60);
        tabela.getColumnModel().getColumn(3).setMaxWidth(70);
        tabela.getColumnModel().getColumn(4).setMinWidth(120);
        tabela.getColumnModel().getColumn(4).setMaxWidth(130);
        tabela.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                mostrarSelecionada();
            }
        });

        JScrollPane rolagem = new JScrollPane(tabela);
        rolagem.setBorder(BorderFactory.createLineBorder(Tema.BORDA));
        rolagem.getViewport().setBackground(Tema.CARTAO);

        JPanel lista = new JPanel(new BorderLayout(0, 10));
        lista.setOpaque(false);
        lista.add(busca, BorderLayout.NORTH);
        lista.add(rolagem, BorderLayout.CENTER);
        return lista;
    }

    private JPanel criarDetalhe() {
        painelDetalhe.setPreferredSize(new Dimension(400, 400));
        painelDetalhe.add(criarEstadoVazio(), "vazio");
        painelDetalhe.add(criarMensagem("Selecione uma doação na lista para ver os itens."), "nenhuma");
        painelDetalhe.add(criarFicha(), "ficha");
        return painelDetalhe;
    }

    private JPanel criarEstadoVazio() {
        JLabel texto = new JLabel("<html><div style='text-align:center'><b>Nenhuma doação por aqui ainda.</b><br><br>"
                + "Clique em <b>+ Nova doação</b> para registrar a primeira,<br>ou carregue alguns exemplos para conhecer o sistema.</div></html>",
                SwingConstants.CENTER);
        texto.setFont(Tema.fonte(Font.PLAIN, 14));
        texto.setForeground(Tema.TEXTO_SUAVE);
        Botao btnExemplos = Botao.secundario("Carregar exemplos");
        btnExemplos.addActionListener(e -> executar(() -> {
            controller.carregarExemplos();
            recarregar(null);
        }));
        JPanel botao = new JPanel(new FlowLayout(FlowLayout.CENTER));
        botao.setOpaque(false);
        botao.add(btnExemplos);

        JPanel vazio = new JPanel(new BorderLayout(0, 10));
        vazio.setBackground(Tema.CARTAO);
        vazio.setBorder(Tema.bordaCartao());
        vazio.add(texto, BorderLayout.CENTER);
        vazio.add(botao, BorderLayout.SOUTH);
        return vazio;
    }

    private JPanel criarMensagem(String mensagem) {
        JLabel texto = new JLabel(mensagem, SwingConstants.CENTER);
        texto.setFont(Tema.fonte(Font.PLAIN, 14));
        texto.setForeground(Tema.TEXTO_SUAVE);
        JPanel painel = new JPanel(new BorderLayout());
        painel.setBackground(Tema.CARTAO);
        painel.setBorder(Tema.bordaCartao());
        painel.add(texto, BorderLayout.CENTER);
        return painel;
    }

    private JPanel criarFicha() {
        lblDoador.setFont(Tema.fonte(Font.BOLD, 19));
        lblDoador.setForeground(Tema.TEXTO);
        lblDestino.setFont(Tema.fonte(Font.PLAIN, 14));
        lblDestino.setForeground(Tema.TEXTO_SUAVE);
        lblSituacao.setFont(Tema.fonte(Font.BOLD, 13));
        lblSituacao.setOpaque(true);
        lblSituacao.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        JPanel linhaSituacao = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 4));
        linhaSituacao.setOpaque(false);
        linhaSituacao.add(lblSituacao);

        JPanel topo = new JPanel(new GridLayout(3, 1, 0, 2));
        topo.setOpaque(false);
        topo.add(lblDoador);
        topo.add(lblDestino);
        topo.add(linhaSituacao);

        JLabel lblItens = new JLabel("Itens desta doação");
        lblItens.setFont(Tema.fonte(Font.BOLD, 13));
        lblItens.setForeground(Tema.TEXTO_SUAVE);
        listaItens.setCellRenderer(new RenderizadorItem());
        listaItens.setFocusable(false);
        JScrollPane rolagem = new JScrollPane(listaItens);
        rolagem.setBorder(BorderFactory.createLineBorder(Tema.BORDA));
        rolagem.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        JPanel itens = new JPanel(new BorderLayout(0, 6));
        itens.setOpaque(false);
        itens.add(lblItens, BorderLayout.NORTH);
        itens.add(rolagem, BorderLayout.CENTER);

        Botao btnEditar = Botao.secundario("Ver / editar");
        Botao btnExcluir = Botao.perigo("Excluir");
        btnEditar.addActionListener(e -> editarSelecionada());
        btnEntregue.addActionListener(e -> entregarSelecionada());
        btnExcluir.addActionListener(e -> excluirSelecionada());
        JPanel secundarias = new JPanel(new GridLayout(1, 2, 8, 0));
        secundarias.setOpaque(false);
        secundarias.add(btnEditar);
        secundarias.add(btnExcluir);
        JPanel acoes = new JPanel(new GridLayout(2, 1, 0, 8));
        acoes.setOpaque(false);
        acoes.add(btnEntregue);
        acoes.add(secundarias);

        JPanel ficha = new JPanel(new BorderLayout(0, 12));
        ficha.setBackground(Tema.CARTAO);
        ficha.setBorder(Tema.bordaCartao());
        ficha.add(topo, BorderLayout.NORTH);
        ficha.add(itens, BorderLayout.CENTER);
        ficha.add(acoes, BorderLayout.SOUTH);
        return ficha;
    }

    // ------------------------------------------------------------------ comportamento

    /** Busca tudo de novo no banco e atualiza a tela. Se receber um id, deixa essa doação selecionada. */
    private void recarregar(Long idParaSelecionar) {
        List<Doacao> doacoes;
        try {
            doacoes = controller.listar();
        } catch (PersistenciaException e) {
            mostrarErro(e.getMessage());
            return;
        }
        modeloTabela.setDoacoes(doacoes);
        atualizarResumo(controller.resumir(doacoes, LocalDate.now()));

        if (idParaSelecionar != null) {
            for (int i = 0; i < modeloTabela.getRowCount(); i++) {
                if (idParaSelecionar.equals(modeloTabela.getDoacao(i).getId())) {
                    int linha = tabela.convertRowIndexToView(i);
                    if (linha >= 0) {
                        tabela.setRowSelectionInterval(linha, linha);
                        tabela.scrollRectToVisible(tabela.getCellRect(linha, 0, true));
                    }
                }
            }
        }
        mostrarSelecionada();
    }

    private void atualizarResumo(ResumoDoacoes resumo) {
        cartaoTotal.setValor(resumo.getTotal());
        cartaoRecebidas.setValor(resumo.getRecebidas());
        cartaoEntregues.setValor(resumo.getEntregues());
        cartaoVencendo.setValor(resumo.getAlimentosVencendo());
        int vencendo = resumo.getAlimentosVencendo();
        faixaAlerta.setVisible(vencendo > 0);
        faixaAlerta.setText((vencendo == 1 ? "1 alimento vence" : vencendo + " alimentos vencem")
                + " nos próximos " + Alimento.DIAS_DE_ALERTA
                + " dias. As doações destacadas em amarelo devem ser entregues primeiro.");
    }

    private void filtrar() {
        String texto = txtBusca.getText().trim();
        if (texto.isEmpty()) {
            ordenador.setRowFilter(null);
        } else {
            ordenador.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(texto), 1, 2));
        }
    }

    private Doacao selecionada() {
        int linha = tabela.getSelectedRow();
        return linha < 0 ? null : modeloTabela.getDoacao(tabela.convertRowIndexToModel(linha));
    }

    private void mostrarSelecionada() {
        Doacao doacao = selecionada();
        if (modeloTabela.getRowCount() == 0) {
            cartoesDetalhe.show(painelDetalhe, "vazio");
            return;
        }
        if (doacao == null) {
            cartoesDetalhe.show(painelDetalhe, "nenhuma");
            return;
        }
        lblDoador.setText(doacao.getDoador());
        lblDestino.setText("Para: " + doacao.getDestino() + "   ·   Doação nº " + doacao.getId());
        boolean entregue = doacao.getSituacao() == Situacao.ENTREGUE;
        lblSituacao.setText(entregue ? "ENTREGUE" : "AGUARDANDO ENTREGA");
        lblSituacao.setBackground(entregue ? new Color(220, 240, 228) : new Color(222, 234, 247));
        lblSituacao.setForeground(entregue ? Tema.VERDE_CLARO : Tema.AZUL);
        btnEntregue.setEnabled(!entregue);

        modeloItens.clear();
        for (ItemDoacao item : doacao.getItens()) {
            modeloItens.addElement(item);
        }
        cartoesDetalhe.show(painelDetalhe, "ficha");
    }

    private void novaDoacao() {
        DialogoDoacao dialogo = new DialogoDoacao(janela, controller, null);
        Doacao salva = dialogo.abrir();
        if (salva != null) {
            txtBusca.setText("");
            recarregar(salva.getId());
        }
    }

    private void editarSelecionada() {
        Doacao doacao = selecionada();
        if (doacao == null) {
            return;
        }
        Doacao salva = new DialogoDoacao(janela, controller, doacao).abrir();
        if (salva != null) {
            recarregar(salva.getId());
        }
    }

    private void entregarSelecionada() {
        Doacao doacao = selecionada();
        if (doacao == null) {
            return;
        }
        int resposta = JOptionPane.showConfirmDialog(janela,
                "Confirmar que a doação de " + doacao.getDoador() + " foi entregue para " + doacao.getDestino() + "?",
                "Registrar entrega", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (resposta == JOptionPane.YES_OPTION) {
            executar(() -> {
                controller.marcarComoEntregue(doacao.getId());
                recarregar(doacao.getId());
            });
        }
    }

    private void excluirSelecionada() {
        Doacao doacao = selecionada();
        if (doacao == null) {
            return;
        }
        int resposta = JOptionPane.showConfirmDialog(janela,
                "Excluir a doação de " + doacao.getDoador() + " e os " + doacao.getItens().size()
                        + " itens dela?\nIsso não pode ser desfeito.",
                "Excluir doação", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (resposta == JOptionPane.YES_OPTION) {
            executar(() -> {
                controller.excluir(doacao.getId());
                recarregar(null);
            });
        }
    }

    /** Executa uma ação e transforma qualquer erro em uma mensagem amigável. */
    private void executar(Runnable acao) {
        try {
            acao.run();
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(janela, e.getMessage(), "Atenção", JOptionPane.WARNING_MESSAGE);
        } catch (PersistenciaException e) {
            mostrarErro(e.getMessage());
        }
    }

    private void mostrarErro(String mensagem) {
        JOptionPane.showMessageDialog(janela, mensagem + "\nConfira se o arquivo data/doaacao.db não está aberto em outro programa.",
                "Problema com o banco de dados", JOptionPane.ERROR_MESSAGE);
    }

    // ------------------------------------------------------------------ renderizadores

    /** Linhas alternadas e destaque amarelo nas doações com alimento vencendo. */
    private class RenderizadorLinha extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable tabela, Object valor, boolean selecionada,
                                                       boolean foco, int linha, int coluna) {
            super.getTableCellRendererComponent(tabela, valor, selecionada, false, linha, coluna);
            int linhaModelo = tabela.convertRowIndexToModel(linha);
            setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
            setHorizontalAlignment(coluna == 0 || coluna == 3 ? SwingConstants.CENTER : SwingConstants.LEFT);
            setFont(Tema.fonte(coluna == 4 ? Font.BOLD : Font.PLAIN, 14));
            if (selecionada) {
                setBackground(Tema.SELECAO);
            } else if (modeloTabela.precisaDeAtencao(linhaModelo)) {
                setBackground(Tema.ALERTA_FUNDO);
            } else {
                setBackground(linha % 2 == 0 ? Tema.CARTAO : Tema.LINHA_ALTERNADA);
            }
            if (coluna == 4) {
                setForeground(Situacao.ENTREGUE.getRotulo().equals(valor) ? Tema.VERDE_CLARO : Tema.AZUL);
            } else {
                setForeground(Tema.TEXTO);
            }
            return this;
        }
    }

    /** Cada item aparece pelo seu resumo(): o Alimento fala da validade, a Higiene da embalagem. */
    private static class RenderizadorItem extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> lista, Object valor, int indice,
                                                      boolean selecionado, boolean foco) {
            ItemDoacao item = (ItemDoacao) valor;
            super.getListCellRendererComponent(lista, item.resumo(), indice, false, false);
            setFont(Tema.fonte(Font.PLAIN, 14));
            setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
            setText("<html><div style='width:250px'><b>" + item.getTipo().getRotulo() + ":</b> "
                    + item.resumo() + "</div></html>");
            boolean atencao = item.precisaDeAtencao(LocalDate.now());
            setBackground(atencao ? Tema.ALERTA_FUNDO : Tema.CARTAO);
            setForeground(atencao ? Tema.ALERTA_TEXTO : Tema.TEXTO);
            return this;
        }
    }
}
