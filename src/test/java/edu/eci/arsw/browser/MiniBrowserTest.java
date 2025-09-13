package edu.eci.arsw.browser;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class MiniBrowserTest {

    static HttpServer server;
    static int port;
    static final String HTML = "<!doctype html><html><body>Hola ARSW</body></html>";

    @BeforeAll
    static void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        port = server.getAddress().getPort();
        server.createContext("/page", exchange -> {
            byte[] data = HTML.getBytes();
            exchange.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, data.length);
            try (var os = exchange.getResponseBody()) { os.write(data); }
        });
        server.start();
    }

    @AfterAll
    static void stop() { server.stop(0); }

    @Test
    void downloadsToFileAndReports() throws IOException {
        String url = "http://127.0.0.1:" + port + "/page";
        Path out = Files.createTempDirectory("browser-test").resolve("resultado.html");
        String outStr = out.toAbsolutePath().toString();

        String stdout = captureStdout(() ->
                MiniBrowser.main(new String[]{"--url=" + url, "--out=" + outStr}));

        assertTrue(Files.exists(out));
        String content = Files.readString(out);
        assertEquals(HTML, content);
        assertTrue(stdout.contains("Descargado: "));
        assertTrue(stdout.contains("Content-Type: "));
        assertTrue(stdout.contains(outStr));
    }

    private static String captureStdout(Runnable r) {
        var old = System.out;
        var baos = new java.io.ByteArrayOutputStream();
        System.setOut(new java.io.PrintStream(baos));
        try { r.run(); } finally { System.setOut(old); }
        return baos.toString();
    }
}
