package com.estebanfcv.tailog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JDesktopPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

/**
 *
 * @author estebanfcv
 */
public class JLogTailerFrame extends JFrame implements Serializable {

    public static final File ARCHIVO_CONFIGURACION = new File(System.getProperties().getProperty("user.home"), "JLogTailer2.xml");
    private final transient JDesktopPane escritorio = new JDesktopPane();
    private File directorioActual = null;

    public JLogTailerFrame(String title, int width, int height) {
        this.setTitle(title);
        this.setSize(width, height);

        Container pane = this.getContentPane();
        pane.add(escritorio, BorderLayout.CENTER);

        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("Archivo");
        menuBar.add(fileMenu);
        JMenuItem fileOpenItem = new JMenuItem("Abrir log");
        fileMenu.add(fileOpenItem);
        JMenuItem fileExitItem = new JMenuItem("Salir");
        fileMenu.add(fileExitItem);

        JMenu windowMenu = new JMenu("Ventana");
        menuBar.add(windowMenu);
        JMenuItem windowTileVerticallyItem = new JMenuItem("Tile vertically");
        windowMenu.add(windowTileVerticallyItem);
        JMenuItem windowTileBoxedItem = new JMenuItem("Tile boxed");
        windowMenu.add(windowTileBoxedItem);

        JMenu helpMenu = new JMenu("Ayuda");
        menuBar.add(helpMenu);

        JMenuItem helpAboutItem = new JMenuItem("Acerca de");
        helpMenu.add(helpAboutItem);

        this.setJMenuBar(menuBar);

        // Allows the window to be closed.
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                salir();
            }
        });

        // Adds a new log tailing internal frame to the desktop.
        fileOpenItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                JFileChooser chooser = new JFileChooser(directorioActual);
                int returnVal = chooser.showOpenDialog(JLogTailerFrame.this);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = new File(chooser.getCurrentDirectory(), chooser.getSelectedFile().getName());
                    leerArchivoConfiguracion(file);
                    directorioActual = file.getParentFile();
                }
            }
        });

        // Exits the application.
        fileExitItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                salir();
            }
        });

        windowTileVerticallyItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                tileInternalFramesVertically();
            }
        });

        windowTileBoxedItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                tileInternalFramesBoxed();
            }
        });

        // Displays "about" information.
        helpAboutItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                JOptionPane.showMessageDialog(JLogTailerFrame.this,
                        "JLogTailer 2.0.0\nA Java log tailer by Paul Mutton\nhttp://www.jibble.org/",
                        "About", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        this.setVisible(true);

    }

    private void startLogging(File file, List rules, Rectangle bounds) throws IOException {
        JLogTailerInternalFrame iFrame = new JLogTailerInternalFrame(JLogTailerFrame.this, file, bounds);
        if (rules != null) {
            List logRules = iFrame.getRules();
            synchronized (logRules) {
                logRules.addAll(rules);
            }
            iFrame.setRules(logRules);
        }
        escritorio.add(iFrame);
        iFrame.moveToFront();
        Thread t = new Thread(iFrame);
        t.start();
    }

    private void leerArchivoConfiguracion(File file) {
        try {
            System.out.println("Leyendo el archivo de configuración...");
            java.beans.XMLDecoder decoder = new java.beans.XMLDecoder(new BufferedInputStream(new FileInputStream(ARCHIVO_CONFIGURACION)));
            this.setBounds((Rectangle) decoder.readObject());
            int frameCount = ((Integer) decoder.readObject());
            for (int i = 0; i < frameCount; i++) {
                decoder.readObject();
                Rectangle bounds = (Rectangle) decoder.readObject();
                int ruleCount = ((Integer) decoder.readObject());
                ArrayList rules = new ArrayList();
                for (int j = 0; j < ruleCount; j++) {
                    String name = decoder.readObject().toString();
                    String condicion = decoder.readObject().toString();
                    boolean subrayado = ((Boolean) decoder.readObject());
                    boolean negritas = ((Boolean) decoder.readObject());
                    boolean filtro = ((Boolean) decoder.readObject());
                    boolean sonido = ((Boolean) decoder.readObject());
                    Color color = (Color) decoder.readObject();
                    CondicionFormato regla = new CondicionFormato(name, condicion, subrayado, negritas, filtro, sonido, color);
                    rules.add(regla);
                }
                startLogging(file, rules, bounds);
            }

            decoder.close();
        } catch (Exception e) {
            System.out.println("Could not find previous configuration: assuming defaults.");
        }
    }

    private void salir() {
        this.setVisible(false);

        // Save our configuration!
        try {
            java.beans.XMLEncoder encoder = new java.beans.XMLEncoder(new BufferedOutputStream(new FileOutputStream(ARCHIVO_CONFIGURACION)));
            encoder.writeObject(this.getBounds());
            JInternalFrame[] frames = escritorio.getAllFrames();
            encoder.writeObject(new Integer(frames.length));
            for (JInternalFrame jFrame : frames) {
                JLogTailerInternalFrame frame = (JLogTailerInternalFrame) jFrame;
                encoder.writeObject(frame.getFile().getPath());
                encoder.writeObject(frame.getBounds());
                List<CondicionFormato> rules = frame.getRules();
                synchronized (rules) {
                    encoder.writeObject(new Integer(rules.size()));
                    for (CondicionFormato cf : rules) {
                        encoder.writeObject(cf.getNombre());
                        encoder.writeObject(cf.getExpresion());
                        encoder.writeObject(cf.isSubrayado());
                        encoder.writeObject(cf.isNegritas());
                        encoder.writeObject(cf.isFiltro());
                        encoder.writeObject(cf.isSonido());
                        encoder.writeObject(cf.getColor());
                    }
                }
            }
            encoder.flush();
            encoder.close();
            System.out.println("Configuration saved.");
        } catch (Exception e) {
            System.out.println("Unable to save configuration for next use: " + e);
        }

        // No need to explictly tidy anything else up as we're only reading files.
        System.exit(0);
    }

    private void tileInternalFramesVertically() {
        int desktopWidth = escritorio.getWidth();
        int desktopHeight = escritorio.getHeight();
        JInternalFrame[] frames = escritorio.getAllFrames();
        int frameCount = frames.length;
        int n=0;
        for (JInternalFrame jFrame : frames) {
            try {
                jFrame.setIcon(false);
            } catch (java.beans.PropertyVetoException e) {
                e.printStackTrace();
            }
            jFrame.reshape(0, (n * desktopHeight) / frameCount, desktopWidth, desktopHeight / frameCount);
            jFrame.setVisible(true);
            jFrame.toFront();
            n++;
        }
    }

    private void tileInternalFramesBoxed() {
        int desktopWidth = escritorio.getWidth();
        int desktopHeight = escritorio.getHeight();
        JInternalFrame[] frames = escritorio.getAllFrames();
        int frameCount = frames.length;
        int totalRows = (int) Math.sqrt((double) frameCount);
        int totalCols = 1;
        while (totalCols * totalRows < frameCount) {
            totalCols++;
        }
        int windowsDrawn = 0;
        for (int row = 0; row < totalRows; row++) {
            for (int col = 0; col < totalCols; col++) {
                if (windowsDrawn == frameCount) {
                    break;
                }
                JInternalFrame frame = frames[row * totalCols + col];
                try {
                    frame.setIcon(false);
                } catch (java.beans.PropertyVetoException e) {
                    // Carry on...
                }
                frame.reshape((col * desktopWidth) / totalCols, (row * desktopHeight) / totalRows, desktopWidth / totalCols, desktopHeight / totalRows);
                frame.setVisible(true);
                frame.toFront();
                windowsDrawn++;
            }
        }
    }
}
