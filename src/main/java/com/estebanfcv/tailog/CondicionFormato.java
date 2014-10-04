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
    private String condicion;
    private boolean subrayado;
    private boolean negritas;
    private boolean filtro;
    private boolean sonido;
    private Color color = null;

    public CondicionFormato() {
        this("Default", "", false, false, false, false, Color.black);
    }

    public CondicionFormato(String nombre, String condicion, boolean subrayado, boolean negritas, boolean filtro,
            boolean sonido,Color color) {
        this.nombre = nombre;
        this.condicion = condicion;
        this.subrayado = subrayado;
        this.negritas = negritas;
        this.sonido = sonido;
        this.filtro = filtro;
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

    public String getCondicion() {
        return condicion;
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

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setCondicion(String condicion) {
        this.condicion = condicion;
    }

    public void setSubrayado(boolean subrayado) {
        this.subrayado = subrayado;
    }

    public void setNegritas(boolean negritas) {
        this.negritas = negritas;
    }

    public void setFiltro(boolean filtro) {
        this.filtro = filtro;
    }

    public void setSonido(boolean sonido) {
        this.sonido = sonido;
    }

    public void setColor(Color color) {
        this.color = color;
    }
}
