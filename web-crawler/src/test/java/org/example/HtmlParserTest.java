package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HtmlParserTest {
    private HtmlParser parser;
    private ByteArrayOutputStream testOut;
    private HtmlDataExtractor mockExtractor; // Unser neuer Mock für das Interface

    @BeforeEach
    void setUp() {
        testOut = new ByteArrayOutputStream();
        mockExtractor = mock(HtmlDataExtractor.class); // Interface mocken statt Jsoup!
        // Dem Parser übergeben wir jetzt den Mock
        parser = new HtmlParser(3, new PrintStream(testOut), mockExtractor);
    }

    @Test
    void testHashtagsForHeadingLevels() {
        assertEquals("#", parser.getHashtagPrefix(1));
        assertEquals("###", parser.getHashtagPrefix(3));
    }

    @Test
    void testIndentationDashes() {
        assertEquals("", parser.getIndentation(1));
        assertEquals("-->", parser.getIndentation(2));
    }

    @Test
    void testBrokenLinkOutputFormatting() {
        testOut.reset();
        parser.addBrokenLinkToReport("http://error.com", 1);
        assertTrue(testOut.toString().contains("<br>broken link"));

        testOut.reset();
        parser.addBrokenLinkToReport("http://error.com", 2);
        assertTrue(testOut.toString().contains("<br>--> broken link"));
    }

    @Test
    void testCrawlStopsCorrecty() {
        parser.crawl("http://test.at", List.of("test.at"), 10); // 10 > 3 (maxDepth)

        parser.visitedURLs.add("http://schon-besucht.at");
        parser.crawl("http://schon-besucht.at", List.of("test.at"), 1);

        assertTrue(testOut.toString().contains("depth: 10") || testOut.toString().isEmpty());
    }

    @Test
    void testHandleLinksSkipsInvalid() throws IOException {
        String url = "http://meine-seite.at";

        // Wir sagen dem Mock einfach, was er zurückgeben soll, wenn er gefragt wird
        when(mockExtractor.extractHeadings(url)).thenReturn(List.of());
        when(mockExtractor.extractLinks(url)).thenReturn(List.of("http://google.com"));

        parser.crawl(url, List.of("meine-seite.at"), 1);

        assertFalse(testOut.toString().contains("google.com"), "Ungültiger Link sollte ignoriert werden");
    }

    @Test
    void testCrawlSuccessWithMock() throws IOException {
        String url = "http://mock-test.at";

        // Simuliere die Rückgabe von Jsoup im sauberen String-Format: "tag:text"
        when(mockExtractor.extractHeadings(url)).thenReturn(List.of("h1:Titel"));
        when(mockExtractor.extractLinks(url)).thenReturn(List.of("http://mock-test.at/page2"));

        parser.crawl(url, List.of("mock-test.at"), 1);

        String output = testOut.toString();
        assertAll("Crawl Validierung",
                () -> assertTrue(output.contains("# Titel"), "Die Überschrift wurde nicht korrekt formatiert gedruckt!"),
                () -> assertTrue(parser.visitedURLs.contains(url), "Die Ausgangs-URL sollte im HashSet als besucht markiert sein.")
        );
    }

    @Test
    void testCrawlThrowsException() throws IOException {
        String url = "http://kaputt.at";
        List<String> domains = List.of("kaputt.at");

        // Wenn das Interface aufgerufen wird, lassen wir es eine IOException werfen
        when(mockExtractor.extractHeadings(url)).thenThrow(new IOException("Simulierter Netzwerkfehler"));

        parser.crawl(url, domains, 1);

        String output = testOut.toString();
        assertTrue(output.contains("broken link"), "Der Crawler sollte eine IOException abfangen und broken link im Report protokollieren.");
    }
}