package edu.eci.arsw.browser;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Optional;

public class MiniBrowser {

    private static String extractArg(String[] args, String key) {
        return Arrays.stream(args)
                .filter(a -> a.startsWith("--"+key+"="))
                .map(a -> a.substring(("--"+key+"=").length()))
                .findFirst().orElse(null);
    }

    private static String askUrlFromStdIn() throws IOException {
        System.out.print("Ingrese una URL: ");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        return br.readLine();
    }

    public static void main(String[] args) {
        try {
            String urlStr = Optional.ofNullable(extractArg(args, "url"))
                    .orElseGet(() -> (args.length > 0 && !args[0].startsWith("--")) ? args[0] : null);

            if (urlStr == null || urlStr.isBlank()) {
                urlStr = askUrlFromStdIn();
            }
            if (urlStr == null || urlStr.isBlank()) {
                System.err.println("No se proporcionó URL.");
                System.exit(2);
            }

            String outPath = Optional.ofNullable(extractArg(args, "out")).orElse("resultado.html");

            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setInstanceFollowRedirects(true);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(10000);

            int status = conn.getResponseCode();
            if (isRedirect(status)) {
                String location = conn.getHeaderField("Location");
                if (location != null) {
                    conn.disconnect();
                    url = new URL(location);
                    conn = (HttpURLConnection) url.openConnection();
                }
            }

            String contentType = conn.getContentType();
            long bytes = 0;
            File outFile = new File(outPath);

            try (InputStream in = new BufferedInputStream(conn.getInputStream());
                 OutputStream out = new BufferedOutputStream(new FileOutputStream(outFile))) {
                byte[] buf = new byte[8192];
                int r;
                while ((r = in.read(buf)) != -1) {
                    out.write(buf, 0, r);
                    bytes += r;
                }
            } finally {
                conn.disconnect();
            }

            System.out.println("Descargado: " + bytes + " bytes");
            System.out.println("Content-Type: " + (contentType != null ? contentType : "desconocido"));
            System.out.println("Archivo: " + outFile.getAbsolutePath());

        } catch (IOException e) {
            System.err.println("Error descargando: " + e.getMessage());
            System.exit(1);
        }
    }

    private static boolean isRedirect(int status) {
        return status == HttpURLConnection.HTTP_MOVED_PERM
                || status == HttpURLConnection.HTTP_MOVED_TEMP
                || status == HttpURLConnection.HTTP_SEE_OTHER
                || status == 307 || status == 308;
    }
}
