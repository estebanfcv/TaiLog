package com.estebanfcv.tailog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

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

    private CondicionFormato _rule = null;

    public CondicionFormatoDialog(Dialog owner, CondicionFormato rule) {
        super(owner);

        jtfNombre = new JTextField(rule.getNombre());
        jtfRegla = new JTextField(rule.getExpresion());
        jcbNegritas.setSelected(rule.isNegritas());
        jcbSubrayado.setSelected(rule.isSubrayado());
        jcbFiltrado.setSelected(rule.isFiltro());
        jcbSonido.setSelected(rule.isSonido());
        jtfColor = new JTextField("#" + Integer.toHexString(rule.getColor().getRGB()).substring(2).toUpperCase());
        Container pane = this.getContentPane();
        JPanel panel = new JPanel(new GridLayout(12, 1));
        pane.add(panel, BorderLayout.CENTER);

        panel.add(jtfNombre);
        panel.add(new JLabel("Si la línea contiene la cadena:"));
        panel.add(jtfRegla);
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
                _rule = new CondicionFormato(jtfNombre.getText(), jtfRegla.getText(), jcbSubrayado.isSelected(),
                        jcbNegritas.isSelected(), jcbFiltrado.isSelected(), jcbSonido.isSelected(), Color.decode(jtfColor.getText()));
                dispose();
            }
        });
        jbColor.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                JColorChooser jcc = new JColorChooser();
                jtfColor.setText("#" + Integer.toHexString(jcc.showDialog(null, "Seleccione un color", Color.BLACK).getRGB())
                        .substring(2).toUpperCase());
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