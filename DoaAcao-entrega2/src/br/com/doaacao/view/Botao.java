package br.com.doaacao.view;

import javax.swing.JButton;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/** Botão arredondado com a mesma aparência no Windows, no Mac e no Linux. */
class Botao extends JButton {

    private final Color fundo;
    private final Color texto;

    private Botao(String rotulo, Color fundo, Color texto) {
        super(rotulo);
        this.fundo = fundo;
        this.texto = texto;
        setFont(Tema.fonte(Font.BOLD, 14));
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        FontMetrics fm = getFontMetrics(getFont());
        setPreferredSize(new Dimension(fm.stringWidth(rotulo) + 36, 38));
    }

    static Botao principal(String rotulo) {
        return new Botao(rotulo, Tema.VERDE_CLARO, Color.WHITE);
    }

    static Botao secundario(String rotulo) {
        return new Botao(rotulo, new Color(232, 229, 221), Tema.TEXTO);
    }

    static Botao perigo(String rotulo) {
        return new Botao(rotulo, new Color(250, 228, 225), Tema.VERMELHO);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        Color cor = fundo;
        if (!isEnabled()) {
            cor = new Color(236, 234, 229);
        } else if (getModel().isPressed()) {
            cor = fundo.darker();
        } else if (getModel().isRollover()) {
            cor = misturar(fundo, Color.WHITE);
        }
        g2.setColor(cor);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);

        g2.setFont(getFont());
        g2.setColor(isEnabled() ? texto : new Color(170, 170, 170));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2,
                (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
        g2.dispose();
    }

    private static Color misturar(Color a, Color b) {
        return new Color((a.getRed() * 4 + b.getRed()) / 5, (a.getGreen() * 4 + b.getGreen()) / 5,
                (a.getBlue() * 4 + b.getBlue()) / 5);
    }
}
