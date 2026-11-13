package br.com.doaacao.view;

import javax.swing.BorderFactory;
import javax.swing.JTextField;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.Font;

/** Cores e fontes do sistema. O amarelo-mostarda é a cor oficial da ODS 2 (Fome Zero). */
final class Tema {

    static final Color VERDE = new Color(32, 96, 70);
    static final Color VERDE_CLARO = new Color(46, 139, 87);
    static final Color MOSTARDA_ODS2 = new Color(221, 166, 58);
    static final Color FUNDO = new Color(246, 244, 239);
    static final Color CARTAO = Color.WHITE;
    static final Color BORDA = new Color(225, 221, 212);
    static final Color TEXTO = new Color(40, 44, 52);
    static final Color TEXTO_SUAVE = new Color(110, 112, 118);
    static final Color ALERTA_FUNDO = new Color(255, 243, 205);
    static final Color ALERTA_TEXTO = new Color(133, 92, 0);
    static final Color VERMELHO = new Color(192, 57, 43);
    static final Color AZUL = new Color(41, 112, 179);
    static final Color LINHA_ALTERNADA = new Color(250, 249, 246);
    static final Color SELECAO = new Color(214, 234, 222);

    private Tema() {
    }

    static Font fonte(int estilo, int tamanho) {
        return new Font("SansSerif", estilo, tamanho);
    }

    static Border bordaCartao() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDA),
                BorderFactory.createEmptyBorder(12, 14, 12, 14));
    }

    static JTextField campo() {
        JTextField campo = new JTextField();
        campo.setFont(fonte(Font.PLAIN, 15));
        campo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDA),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)));
        return campo;
    }
}
