package com.estebanfcv.tailog;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;

/**
 * 
 * @author estebanfcv
 */
public class AutoScrollTextArea extends JScrollPane {
    
    public AutoScrollTextArea() {
        super();
        _textPane.setEditable(false);
        _textPane.setFont(new Font("Monospaced", Font.PLAIN, 12));
        this.setViewportView(_textPane);
    }
    
    private void scrollToBottom() {
        _textPane.setCaretPosition(_textPane.getDocument().getLength());
    }
    
    public JTextPane getTextPane() {
        return _textPane;
    }
    
    public void append(String str) throws BadLocationException {
        _textPane.getDocument().insertString(_textPane.getDocument().getLength(), str, _attributeSet);
        scrollToBottom();
    }
    
    public Document getDocument() {
        return _textPane.getDocument();
    }
    
    public SimpleAttributeSet getSimpleAttributeSet() {
        return _attributeSet;
    }
    
    private JTextPane _textPane = new JTextPane(new DefaultStyledDocument());
    private SimpleAttributeSet _attributeSet = new SimpleAttributeSet();
}