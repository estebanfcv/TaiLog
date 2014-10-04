package com.estebanfcv.util;

import com.estebanfcv.tailog.CondicionFormato;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author estebanfcv
 */
public class Cache {

    private static Map<String, CondicionFormato> mapaCondiciones = new LinkedHashMap<>();

    public static void llenarMapaCondiciones(List<CondicionFormato> listaCondiciones) {
        for (CondicionFormato cf : listaCondiciones) {
            mapaCondiciones.put(cf.getNombre(), cf);
        }
    }

    public static void agregarCondicion(CondicionFormato cf) {
        mapaCondiciones.put(cf.getNombre(), cf);
    }

    public static void eliminarCondicion(String llave) {
        mapaCondiciones.remove(llave);
    }

    public static boolean existeCondicion(String llave) {
        return mapaCondiciones.containsKey(llave);
    }

}
