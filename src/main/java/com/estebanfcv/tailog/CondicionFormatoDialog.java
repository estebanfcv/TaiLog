package com.estebanfcv.tailog;

import com.estebanfcv.util.Cache;
import javax.swing.*;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.event.*;
import javax.swing.colorchooser.AbstractColorChooserPanel;

/**
 *
 * @author estebanfcv
 */
public class CondicionFormatoDialog extends JDialog {

    private JTextField jtfRegla;
    private JTextField jtfColor;
    private JTextField jtfNombre;

    private JCheckBox jcbNegritas = new JCheckBox("Línea en Negritas");
    private JCheckBox jcbSubrayado = new JCheckBox("Subrayar la línea");
    private JCheckBox jcbFiltrado = new JCheckBox("No mostrar la línea");
    private JCheckBox jcbSonido = new JCheckBox("Hacer un sonido");

    private final JButton jbAceptar = new JButton("Aceptar");
    private final JButton jbColor = new JButton("Color de la línea:");

    private JLabel jlErrorNombre = new JLabel("");
    private JLabel jlErrorCondicion = new JLabel("");

    private CondicionFormato _rule = null;

    public CondicionFormatoDialog(Dialog owner, CondicionFormato rule) {
        super(owner);

        jtfNombre = new JTextField(rule.getNombre());
        jtfRegla = new JTextField(rule.getExpresion());
        jcbNegritas.setSelected(rule.isNegritas());
        jcbSubrayado.setSelected(rule.isSubrayado());
        jcbFiltrado.setSelected(rule.isFiltro());
        jcbSonido.setSelected(rule.isSonido());
        jtfColor = new JTextField();
        jtfColor.setEditable(false);
        jtfColor.setBackground(rule.getColor());
        jlErrorNombre.setForeground(Color.red);
        jlErrorCondicion.setForeground(Color.red);
        Container pane = this.getContentPane();
        JPanel panel = new JPanel(new GridLayout(13, 1));
        pane.add(panel, BorderLayout.CENTER);

        panel.add(jtfNombre);
        panel.add(jlErrorNombre);
        panel.add(new JLabel("Si la línea contiene la cadena:"));
        panel.add(jtfRegla);
        panel.add(jlErrorCondicion);
        panel.add(new JLabel("entonces:"));
        panel.add(jcbNegritas);
        panel.add(jcbSubrayado);
        panel.add(jcbFiltrado);
        panel.add(jcbSonido);
        panel.add(jbColor);
        panel.add(jtfColor);
        panel.add(jbAceptar);

        jbAceptar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                jlErrorNombre.setText("");
                jlErrorCondicion.setText("Escribe la condición");
                if (jtfNombre.getText().contains("&")) {
                    jlErrorNombre.setText("No se permite el símbolo &");
                    return;
                }
                if (Cache.existeCondicion(jtfNombre.getText())) {
                    jlErrorNombre.setText("El nombre ya existe");
                    return;
                }

                if (jtfNombre.getText().trim().isEmpty()) {
                    jlErrorNombre.setText("Escribe el nombre");
                    return;
                }
                if (jtfRegla.getText().trim().isEmpty()) {
                    jlErrorCondicion.setText("Escribe la condición");
                    return;
                }
                _rule = new CondicionFormato(jtfNombre.getText(), jtfRegla.getText(), jcbSubrayado.isSelected(),
                        jcbNegritas.isSelected(), jcbFiltrado.isSelected(), jcbSonido.isSelected(), jtfColor.getBackground());
                Cache.agregarCondicion(_rule);
                dispose();
            }
        });
        jbColor.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                JColorChooser jcc = new JColorChooser();
                AbstractColorChooserPanel[] panels = jcc.getChooserPanels();
                AbstractColorChooserPanel[] panels2 = new AbstractColorChooserPanel[1];
                for (AbstractColorChooserPanel panel1 : panels) {
                    if (panel1.getDisplayName().equals("Muestras")) {
                        panels2[0] = panel1;
                    }
                }
                jcc.setChooserPanels(panels2);
                Color c = jcc.showDialog(null, "Seleccione un color", Color.BLACK);
                if (c == null) {
                    c = new Color(Color.BLACK.getRGB());
                }
                jtfColor.setBackground(c);

            }
        });
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        this.pack();
        this.setTitle("Condiciones");
        this.setModal(true);
        this.setResizable(false);
        this.setVisible(true);
    }

    public CondicionFormato getRule() {
        return _rule;
    }
}
