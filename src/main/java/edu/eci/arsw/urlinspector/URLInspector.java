package edu.eci.arsw.urlinspector;

import java.net.MalformedURLException;
import java.net.URL;

public class URLInspector {
    public static void main(String[] args) {
        // Acepta: --url=<...> o primer argumento directo, al estilo simple del ejemplo
        String urlStr = null;
        if (args.length > 0) {
            urlStr = args[0];
            if (urlStr.startsWith("--url=")) {
                urlStr = urlStr.substring("--url=".length());
            }
        }
        if (urlStr == null || urlStr.isBlank()) {
            // Por compatibilidad con el ejemplo, damos un valor por defecto sencillo
            urlStr = "http://www.google.com/";
        }

        try {
            URL u = new URL(urlStr);
            // Imprime los componentes en líneas separadas (como pedía el enunciado)
            System.out.println(u.getProtocol());
            System.out.println(u.getAuthority());
            System.out.println(u.getHost());
            System.out.println(u.getPort());
            System.out.println(u.getPath());
            System.out.println(u.getQuery());
            System.out.println(u.getFile());
            System.out.println(u.getRef());
        } catch (MalformedURLException e) {
            System.err.println("URL inválida: " + e.getMessage());
        }
    }
}
