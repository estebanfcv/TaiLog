package com.estebanfcv.tailog;

import com.estebanfcv.util.Cache;
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
import static com.estebanfcv.util.Util.toInt;

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

        Container container = this.getContentPane();
        container.add(escritorio, BorderLayout.CENTER);

        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("Archivo");
        menuBar.add(fileMenu);
        JMenuItem fileOpenItem = new JMenuItem("Abrir log");
        fileMenu.add(fileOpenItem);
        JMenuItem fileExitItem = new JMenuItem("Salir");
        fileMenu.add(fileExitItem);

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
                    File archivoLog = new File(chooser.getCurrentDirectory(), chooser.getSelectedFile().getName());
                    leerArchivoConfiguracion(archivoLog);
                    directorioActual = archivoLog.getParentFile();
                }
            }
        });

        // Exits the application.
        fileExitItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                salir();
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

    private void iniciarLog(File archivoLog, List<CondicionFormato> rules, Rectangle bounds) throws IOException {
        JLogTailerInternalFrame iFrame = new JLogTailerInternalFrame(JLogTailerFrame.this, archivoLog, bounds);
        if (rules != null) {
            List<CondicionFormato> listaReglas = iFrame.getRules();
            synchronized (listaReglas) {
                listaReglas.addAll(rules);
            }
            iFrame.setRules(listaReglas);
        }
        escritorio.add(iFrame);
        iFrame.moveToFront();
        Thread t = new Thread(iFrame);
        t.start();
    }

    private void leerArchivoConfiguracion(File archivoLog) {

        FileReader fr = null;
        BufferedReader br = null;
        ArrayList<CondicionFormato> listaReglas = new ArrayList();
        try {
            if (!ARCHIVO_CONFIGURACION.exists()) {
                crearArchivoConfiguracion();
            }
            if (!ARCHIVO_CONFIGURACION.exists()) {
                return;
            }
            fr = new FileReader(ARCHIVO_CONFIGURACION);
            br = new BufferedReader(fr);
            String linea;
            while ((linea = br.readLine()) != null) {
                for (StringTokenizer token = new StringTokenizer(linea, "&"); token.hasMoreTokens();) {
                    CondicionFormato cf = new CondicionFormato();
                    cf.setNombre(token.nextToken());
                    cf.setCondicion(token.nextToken());
                    cf.setSubrayado(Boolean.parseBoolean(token.nextToken()));
                    cf.setNegritas(Boolean.parseBoolean(token.nextToken()));
                    cf.setFiltro(Boolean.parseBoolean(token.nextToken()));
                    cf.setSonido(Boolean.parseBoolean(token.nextToken()));
                    cf.setColor(new Color(toInt(token.nextToken()), toInt(token.nextToken()), toInt(token.nextToken())));
                    listaReglas.add(cf);
                }
            }
            Cache.llenarMapaCondiciones(listaReglas);
            iniciarLog(archivoLog, listaReglas, new Rectangle(0, 0, 640, 480));
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
            for (JInternalFrame jFrame : escritorio.getAllFrames()) {
                JLogTailerInternalFrame frame = (JLogTailerInternalFrame) jFrame;
                List<CondicionFormato> listaReglas = frame.getRules();
                synchronized (listaReglas) {
                    for (CondicionFormato cf : listaReglas) {
                        regla = new StringBuilder();
                        regla.append(cf.getNombre()).append("&");
                        regla.append(cf.getCondicion()).append("&");
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
