package com.estebanfcv.tailog;

import java.awt.Color;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;


/**
 *
 * @author estebanfcv
 */
public class CondicionFormato implements java.io.Serializable {

    private String nombre;
    private String expresion;
    private boolean subrayado;
    private boolean negritas;
    private boolean filtro;
    private boolean sonido;
    private Color color = null;

    public CondicionFormato() {
        this("Default", "", false, false, false, false, Color.black);
    }

    public CondicionFormato(String name, String regexp, boolean underlined, boolean bold, boolean filtered, boolean beep,
            Color color)  {
        nombre = name;
        expresion = regexp;
        subrayado = underlined;
        negritas = bold;
        sonido = beep;
        filtro = filtered;
        this.color = color;
    }

    public void alterAttributeSet(SimpleAttributeSet attr) {
        StyleConstants.setForeground(attr, color);
        StyleConstants.setBold(attr, negritas);
        StyleConstants.setUnderline(attr, subrayado);
    }

    public String getNombre() {
        return nombre;
    }

    public String getExpresion() {
        return expresion;
    }

    public boolean isSubrayado() {
        return subrayado;
    }

    public boolean isNegritas() {
        return negritas;
    }

    public boolean isFiltro() {
        return filtro;
    }

    public boolean isSonido() {
        return sonido;
    }

    public Color getColor() {
        return color;
    }

    @Override
    public String toString() {
        return getNombre();
    }

}
