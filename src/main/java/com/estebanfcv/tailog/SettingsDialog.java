package com.estebanfcv.tailog;

import com.estebanfcv.util.Cache;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 *
 * @author estebanfcv
 */
public class SettingsDialog extends JDialog {

    private JList list;
    private JLogTailerInternalFrame _tailer;

    private final JButton nuevaRegla = new JButton("Nueva");
    private final JButton modificarRegla = new JButton("Modificar");
    private final JButton elminarRegla = new JButton("Eliminar");
    private final JButton moverArriba = new JButton("Mover arriba");
    private final JButton moverAbajo = new JButton("Mover abajo");
    private final JButton aceptar = new JButton("Aceptar");

    public SettingsDialog(Frame owner, JLogTailerInternalFrame tailer) {
        super(owner);
        _tailer = tailer;

        list = new JList(_tailer.getRules().toArray());

        Container pane = this.getContentPane();

        JPanel rulesPanel = new JPanel(new BorderLayout());
        JPanel buttonsPanel = new JPanel(new GridLayout(5, 1));

        buttonsPanel.add(nuevaRegla);
        buttonsPanel.add(modificarRegla);
        buttonsPanel.add(elminarRegla);
        buttonsPanel.add(moverArriba);
        buttonsPanel.add(moverAbajo);

        nuevaRegla.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                CondicionFormato rule = new CondicionFormato("Nueva regla", "", false, false, false, false, Color.black);
                CondicionFormatoDialog dialog = new CondicionFormatoDialog(SettingsDialog.this, rule);
                rule = dialog.getRule();

                if (rule != null) {
                    List<CondicionFormato> rules = _tailer.getRules();
                    rules.add(0, rule);
                    list.setListData(rules.toArray());
                }
            }
        });

        modificarRegla.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                int selection = list.getSelectedIndex();
                if (selection >= 0) {
                    List<CondicionFormato> rules = _tailer.getRules();
                    CondicionFormato rule = (CondicionFormato) rules.get(selection);
                    CondicionFormatoDialog dialog = new CondicionFormatoDialog(SettingsDialog.this, rule);
                    if (dialog.getRule() != null) {
                        rules.set(selection, dialog.getRule());
                    }
                    list.setListData(rules.toArray());
                }
            }
        });

        elminarRegla.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                int selection = list.getSelectedIndex();
                if (selection >= 0) {
                    List<CondicionFormato> rules = _tailer.getRules();
                    Cache.eliminarCondicion(rules.get(selection).getNombre());
                    rules.remove(selection);
                    list.setListData(rules.toArray());
                }
            }
        });

        moverArriba.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                int selection = list.getSelectedIndex();
                if (selection > 0) {
                    List<CondicionFormato> rules = _tailer.getRules();
                    rules.set(selection, rules.get(selection - 1));
                    rules.set(selection - 1, (CondicionFormato) rules.get(selection));
                    list.setListData(rules.toArray());
                    list.setSelectedIndex(selection - 1);
                }
            }
        });

        moverAbajo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                int selection = list.getSelectedIndex();
                List<CondicionFormato> rules = _tailer.getRules();
                if (selection < rules.size() - 1) {
                    rules.set(selection + 1, rules.get(selection));
                    rules.set(selection, (CondicionFormato) rules.get(selection + 1));
                    list.setListData(rules.toArray());
                    list.setSelectedIndex(selection + 1);
                }
            }
        });

        aceptar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                dispose();
            }
        });

        JScrollPane scroller = new JScrollPane(list);
        scroller.setPreferredSize(new Dimension(200, 300));

        rulesPanel.add(scroller, BorderLayout.CENTER);
        rulesPanel.add(buttonsPanel, BorderLayout.EAST);
        rulesPanel.add(aceptar, BorderLayout.SOUTH);

        pane.add(rulesPanel, BorderLayout.CENTER);

        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        this.pack();
        this.setTitle("Opciones " + _tailer.getFilename());
        this.setModal(true);
        this.setResizable(false);
        this.setVisible(true);
    }
}
