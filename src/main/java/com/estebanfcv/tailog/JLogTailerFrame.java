package com.estebanfcv.tailog;

import com.estebanfcv.util.Util;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
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

    public static final File ARCHIVO_CONFIGURACION = new File(System.getProperties().getProperty("user.home"), "TaiLog.txt");
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

        FileReader fr = null;
        BufferedReader br = null;
        ArrayList<CondicionFormato> rules = new ArrayList();
        try {
            if (!ARCHIVO_CONFIGURACION.exists()) {
                crearArchivoConfiguracion();
            }
            fr = new FileReader(ARCHIVO_CONFIGURACION);
            br = new BufferedReader(fr);
            String linea;
            while ((linea = br.readLine()) != null) {
                for (StringTokenizer token = new StringTokenizer(linea, "&"); token.hasMoreTokens();) {
                    String nombre = token.nextToken();
                    String condicion = token.nextToken();
                    boolean subrayado = Boolean.parseBoolean(token.nextToken());
                    boolean negritas = Boolean.parseBoolean(token.nextToken());
                    boolean filtro = Boolean.parseBoolean(token.nextToken());
                    boolean sonido = Boolean.parseBoolean(token.nextToken());
                    int r = Integer.parseInt(token.nextToken());
                    int g = Integer.parseInt(token.nextToken());
                    int b = Integer.parseInt(token.nextToken());
                    Color color = new Color(r, g, b);
                    CondicionFormato regla = new CondicionFormato(nombre, condicion, subrayado, negritas, filtro, sonido, color);
                    rules.add(regla);
                }
            }
            startLogging(file, rules, new Rectangle(0, 0, 640, 480));
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Could not find previous configuration: assuming defaults.");
        } finally {
            Util.cerrarLecturaEscritura(br, fr);
        }
    }

    private void salir() {
        this.setVisible(false);
        FileWriter fw = null;
        PrintWriter pw = null;
        StringBuilder regla;
        try {
            fw = new FileWriter(ARCHIVO_CONFIGURACION, false);
            pw = new PrintWriter(fw);
            JInternalFrame[] frames = escritorio.getAllFrames();
            for (JInternalFrame jFrame : frames) {
                JLogTailerInternalFrame frame = (JLogTailerInternalFrame) jFrame;
                List<CondicionFormato> rules = frame.getRules();
                synchronized (rules) {
                    for (CondicionFormato cf : rules) {
                        regla = new StringBuilder();
                        regla.append(cf.getNombre()).append("&");
                        regla.append(cf.getExpresion()).append("&");
                        regla.append(cf.isSubrayado()).append("&");
                        regla.append(cf.isNegritas()).append("&");
                        regla.append(cf.isFiltro()).append("&");
                        regla.append(cf.isSonido()).append("&");
                        regla.append(cf.getColor().getRed()).append("&");
                        regla.append(cf.getColor().getGreen()).append("&");
                        regla.append(cf.getColor().getBlue()).append("\n");
                        pw.append(regla);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Unable to save configuration for next use: " + e);
        } finally {
            Util.cerrarLecturaEscritura(pw, fw);
        }
        System.exit(0);
    }

    private void tileInternalFramesVertically() {
        int desktopWidth = escritorio.getWidth();
        int desktopHeight = escritorio.getHeight();
        JInternalFrame[] frames = escritorio.getAllFrames();
        int frameCount = frames.length;
        int n = 0;
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

    private void crearArchivoConfiguracion() {
        FileWriter fw = null;
        PrintWriter pw = null;
        try {
            fw = new FileWriter(ARCHIVO_CONFIGURACION);
            pw = new PrintWriter(fw);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Util.cerrarLecturaEscritura(pw, fw);
        }
    }
}
