package org.example;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WebCrawlerAppIntegrationTest {

    private HttpServer server;
    private final Path reportPath = Path.of("report.md");
    private final Path htmlSamplesDir = Path.of("html-samples");

    @BeforeEach
    void setUp() throws IOException {

        Files.deleteIfExists(reportPath);


        server = HttpServer.create(new InetSocketAddress(8001), 0);

        server.createContext("/", exchange -> {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/")) path = "/index.html";

            Path file = htmlSamplesDir.resolve(path.substring(1));

            if (Files.exists(file)) {
                byte[] response = Files.readAllBytes(file);


                exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");

                exchange.sendResponseHeaders(200, response.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response);
                }
            } else {

                exchange.sendResponseHeaders(404, 0);
                exchange.close();
            }
        });

        server.start();
    }

    @AfterEach
    void tearDown() throws IOException {
        if (server != null) {
            server.stop(0);
        }
        Files.deleteIfExists(reportPath);
    }

    @Test
    void testFullCrawlIntegrationWithLocalFiles() throws Exception {

        String[] testArgs = {
                "http://localhost:8001/index.html",
                "2",
                "localhost"
        };


        WebCrawlerApp.main(testArgs);


        assertTrue(Files.exists(reportPath), "Die Datei report.md hätte generiert werden müssen.");


        List<String> lines = Files.readAllLines(reportPath);
        String fullContent = String.join("\n", lines);


        assertTrue(fullContent.contains("Willkommen auf der Test-Webseite"), "index.html wurde nicht gecrawlt.");
        assertTrue(fullContent.contains("Über uns"), "seite1.html wurde nicht gecrawlt.");
        assertTrue(fullContent.contains("Unsere Leistungen"), "seite2.html wurde nicht gecrawlt.");


        assertTrue(fullContent.contains("broken link") || fullContent.contains("gibts-nicht.html"),
                "Der fehlerhafte Link wurde nicht korrekt protokolliert.");
    }
}