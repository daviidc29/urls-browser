package edu.eci.arsw.urlinspector;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class URLInspectorTest {

    @Test
    void givenFirstArg_whenRun_printsExpectedComponents() {
        String testUrl = "http://127.0.0.1:8080/foo?x=1#ref";
        IO io = capture(() -> URLInspector.main(new String[]{testUrl}));

        String[] lines = io.out.trim().split("\\R");
        assertEquals(8, lines.length);
        assertEquals("http", lines[0]);                   // protocol
        assertEquals("127.0.0.1:8080", lines[1]);         // authority
        assertEquals("127.0.0.1", lines[2]);              // host
        assertEquals("8080", lines[3]);                   // port
        assertEquals("/foo", lines[4]);                   // path
        assertEquals("x=1", lines[5]);                    // query
        assertEquals("/foo?x=1", lines[6]);               // file
        assertEquals("ref", lines[7]);                    // ref

        assertTrue(io.err.isBlank(), "stderr debe estar vacío para URL válida");
    }

    @Test
    void givenUrlFlag_whenRun_printsExpectedComponents() {
        String testUrl = "http://example.com:8080/a/b?y=2#frag";
        IO io = capture(() -> URLInspector.main(new String[]{"--url=" + testUrl}));

        String[] lines = io.out.trim().split("\\R");
        assertEquals(8, lines.length);
        assertEquals("http", lines[0]);
        assertEquals("example.com:8080", lines[1]);
        assertEquals("example.com", lines[2]);
        assertEquals("8080", lines[3]);
        assertEquals("/a/b", lines[4]);
        assertEquals("y=2", lines[5]);
        assertEquals("/a/b?y=2", lines[6]);
        assertEquals("frag", lines[7]);

        assertTrue(io.err.isBlank(), "stderr debe estar vacío para URL válida");
    }

    @Test
    void whenNoArgs_defaultsToGoogle() {
        IO io = capture(() -> URLInspector.main(new String[]{}));

        String[] lines = io.out.trim().split("\\R");
        assertEquals(8, lines.length);
        assertEquals("http", lines[0]);                   // protocol
        assertEquals("www.google.com", lines[1]);         // authority
        assertEquals("www.google.com", lines[2]);         // host
        assertEquals("-1", lines[3]);                     // port (-1 sin puerto explícito)
        assertEquals("/", lines[4]);                      // path
        assertEquals("null", lines[5]);                   // query
        assertEquals("/", lines[6]);                      // file
        assertEquals("null", lines[7]);                   // ref

        assertTrue(io.err.isBlank(), "stderr debe estar vacío con URL por defecto válida");
    }

    @Test
    void givenMalformedUrl_printsErrorToStderr() {
        // Falta el ':' tras 'http' -> URL malformada
        IO io = capture(() -> URLInspector.main(new String[]{"http//missing-colon.example.com"}));

        assertTrue(io.out.isBlank(), "stdout debe estar vacío cuando la URL es inválida");
        assertFalse(io.err.isBlank(), "stderr debe contener el mensaje de error");
        assertTrue(io.err.trim().startsWith("URL inválida:"));
    }

    // -------- utilidades de captura I/O --------

    private static IO capture(Runnable r) {
        var oldOut = System.out;
        var oldErr = System.err;
        var bout = new java.io.ByteArrayOutputStream();
        var berr = new java.io.ByteArrayOutputStream();
        System.setOut(new java.io.PrintStream(bout));
        System.setErr(new java.io.PrintStream(berr));
        try {
            r.run();
        } finally {
            System.setOut(oldOut);
            System.setErr(oldErr);
        }
        IO io = new IO();
        io.out = bout.toString();
        io.err = berr.toString();
        return io;
    }

    private static class IO {
        String out;
        String err;
    }
}
