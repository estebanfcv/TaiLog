package com.estebanfcv.tailog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JTextPane;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

/**
 *
 * @author estebanfcv
 */
public class JLogTailerInternalFrame extends JInternalFrame implements Runnable, Serializable {

    // Maximum number of lines that we shall display before removing earlier ones.
    private final int lineasMaximas = 500;
    private int lineasMostradas = 0;

    private boolean corriendo = true;
    private final int dormir = 1000;
    private final File file;
    private long filePointer;
    private final AutoScrollTextArea asta = new AutoScrollTextArea();

    private List<CondicionFormato> rules = new ArrayList();

    private final CondicionFormato defaultRule = new CondicionFormato();

    private JFrame _owner;

    private boolean suspender = false;

    public JLogTailerInternalFrame(JFrame owner, File file, Rectangle bounds) throws IOException {
        _owner = owner;
        this.file = file;

        Container pane = this.getContentPane();
        pane.add(asta, BorderLayout.CENTER);
        this.setResizable(true);
        this.setClosable(true);
        this.setMaximizable(true);
        this.setIconifiable(true);
        this.setBounds(bounds);
        this.setTitle(file.getName());
        this.setDefaultCloseOperation(JInternalFrame.DO_NOTHING_ON_CLOSE);

        // Detects when the window is closed.
        this.addInternalFrameListener(new InternalFrameAdapter() {
            public void internalFrameClosing(InternalFrameEvent ife) {
                corriendo = false;
                dispose();
            }
        });

        // Set up the menu bar
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("Archivo");
        menuBar.add(fileMenu);
        JMenu menuFormato = new JMenu("Formato");
        menuBar.add(menuFormato);

        JMenuItem menuCerrar = new JMenuItem("Cerrar");
        fileMenu.add(menuCerrar);
        final JMenuItem iniciar = new JMenuItem("Iniciar");
        fileMenu.add(iniciar);
        iniciar.setVisible(false);
        final JMenuItem detener = new JMenuItem("Detener");
        fileMenu.add(detener);
        detener.setVisible(!suspender);
        JMenuItem menuOpciones = new JMenuItem("Opciones");
        menuFormato.add(menuOpciones);

        this.setJMenuBar(menuBar);

        menuCerrar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                corriendo = false;
                dispose();
            }
        });
        iniciar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                iniciar.setVisible(false);
                detener.setVisible(true);
                System.out.println("[INFO] El hilo está reanudando...");
                corriendo = true;

//                suspender = false;
//                notify();
            }
        });
        detener.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                detener.setVisible(false);
                iniciar.setVisible(true);
                System.out.println("[INFO] El hilo está suspendido...");
//                suspender = true;
                corriendo = false;
            }
        });

        menuOpciones.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                new SettingsDialog(_owner, JLogTailerInternalFrame.this);
            }
        });

        // Do not allow tail logging of non-existant files. (Is this a good idea?)
        if (!file.exists() || file.isDirectory() || !file.canRead()) {
            throw new IOException("No se puede leer el archivo");
        }

        filePointer = this.file.length();

        this.appendMessage("Log tailing started on " + this.file.toString());
        this.setVisible(true);
    }

    // This is the method that contains all the actual log tailing stuff.
    // Note: I'm not particularly happy about the use of the readLine()
    // method call, as it may return a partial line if it reaches the
    // end of the file.  It might be worth jibbling about with this at
    // a later date so that a different approach is used.
    public void run() {
        try {
            while (true) {
                System.out.println("corriendo es:::::::::: "+corriendo);
                if (corriendo) {
                    System.out.println("ENTREEEEEEEEEEEEEEEEEEEEEE");
                    Thread.sleep(dormir);
                    long len = file.length();
                    if (len < filePointer) {
                        // Log must have been jibbled or deleted.
                        this.appendMessage("Log file was reset. Restarting logging from start of file.");
                        filePointer = len;
                    } else if (len > filePointer) {
                        // File must have had something added to it!
                        RandomAccessFile raf = new RandomAccessFile(file, "r");
                        raf.seek(filePointer);
                        String line;
                        while ((line = raf.readLine()) != null) {
                            System.out.println("La linea es:::::: " + line);
                            this.appendLine(line);
                        }
                        filePointer = raf.getFilePointer();
                        raf.close();
                    }
//                synchronized (this) {
//                    while (suspender) {
//                        System.out.println("suspender es:::: "+suspender);
//                        wait();
//                    }
//                }
                }
            }

        } catch (Exception e) {
            this.appendMessage("Fatal error reading log file, log tailing has stopped.");
        }
        dispose();
    }

    public void appendLine(String line) {
        try {

            CondicionFormato rule = defaultRule;
            // Synchronize on the rule list so that nothing can be added to it
            // while we go through it (going through it with a for loop is
            // actually quicker than using an iterator on any other kind of List.
            synchronized (rules) {
                for (CondicionFormato candidate : rules) {
                    if (line.contains(candidate.getCondicion())) {
                        rule = candidate;
                        break;
                    }
                }
            }

            if (rule.isSonido()) {
                // We should beep when this line is seen.
                getToolkit().beep();
            }

            if (rule.isFiltro()) {
                // We're not actually going to show filtered lines...
                return;
            }

            JTextPane textPane = asta.getTextPane();
            Document document = asta.getDocument();
            SimpleAttributeSet attr = asta.getSimpleAttributeSet();

            rule.alterAttributeSet(attr);
            asta.append(line + "\n");

            textPane.setDocument(document);
            if (++lineasMostradas > lineasMaximas) {
                // We must remove a line!
                int len = textPane.getText().indexOf('\n');
                document.remove(0, len);
                lineasMostradas--;
            }
        } catch (BadLocationException e) {
            // But this'll never happen, right?
            throw new RuntimeException("Tried to add a new line to a bad place.");
        }
    }

    public void appendMessage(String message) {
        SimpleAttributeSet attr = asta.getSimpleAttributeSet();
        StyleConstants.setForeground(attr, Color.red);
        this.appendLine("[" + new Date() + ", " + message + "]");
        StyleConstants.setForeground(attr, Color.black);
    }

    public String getFilename() {
        return file.toString();
    }

    public List<CondicionFormato> getRules() {
        return rules;
    }

    public File getFile() {
        return file;
    }

    public void setRules(List<CondicionFormato> rules) {
        this.rules = rules;
    }
}
