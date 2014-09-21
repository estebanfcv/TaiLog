package com.estebanfcv.tailog;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;

/**
 *
 * @author estebanfcv
 */
public class AutoScrollTextArea extends JScrollPane {

    private final JTextPane textPane = new JTextPane(new DefaultStyledDocument());
    private SimpleAttributeSet attributeSet = new SimpleAttributeSet();

    public AutoScrollTextArea() {
        super();
        textPane.setEditable(false);
        textPane.setContentType("text/html;charset=UTF-8");
        textPane.setFont(new Font("Monospaced", Font.PLAIN, 12));
        this.setViewportView(textPane);
    }

    private void scrollToBottom() {
        textPane.setCaretPosition(textPane.getDocument().getLength());
    }

    public JTextPane getTextPane() {
        return textPane;
    }

    public void append(String str) throws BadLocationException {
        textPane.getDocument().insertString(textPane.getDocument().getLength(), str, attributeSet);
        scrollToBottom();
    }

    public Document getDocument() {
        return textPane.getDocument();
    }

    public SimpleAttributeSet getSimpleAttributeSet() {
        return attributeSet;
    }

}
