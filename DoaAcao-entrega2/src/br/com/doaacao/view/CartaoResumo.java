package br.com.doaacao.view;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

/** Cartão do painel de resumo: um número grande com uma legenda embaixo. */
class CartaoResumo extends JPanel {

    private final JLabel lblNumero = new JLabel("0");

    CartaoResumo(String legenda, Color corDoNumero) {
        super(new BorderLayout(0, 2));
        setBackground(Tema.CARTAO);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, corDoNumero),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(1, 0, 1, 1, Tema.BORDA),
                        BorderFactory.createEmptyBorder(10, 14, 10, 14))));
        lblNumero.setFont(Tema.fonte(Font.BOLD, 28));
        lblNumero.setForeground(corDoNumero);
        JLabel lblLegenda = new JLabel(legenda);
        lblLegenda.setFont(Tema.fonte(Font.PLAIN, 13));
        lblLegenda.setForeground(Tema.TEXTO_SUAVE);
        add(lblNumero, BorderLayout.CENTER);
        add(lblLegenda, BorderLayout.SOUTH);
    }

    void setValor(int valor) {
        lblNumero.setText(String.valueOf(valor));
    }
}
