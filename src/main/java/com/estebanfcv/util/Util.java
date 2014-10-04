package com.estebanfcv.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;

/**
 *
 * @author estebanfcv
 */
public class Util {
    
     public static void cerrarLecturaEscritura(BufferedReader br, FileReader fr) {
        try {
            if (br != null) {
                br.close();
            }

            if (fr != null) {
                fr.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
     
     public static void cerrarLecturaEscritura(PrintWriter pw, FileWriter fw) {
        try {
            if (pw != null) {
                pw.close();
            }
            if (fw != null) {
                fw.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
