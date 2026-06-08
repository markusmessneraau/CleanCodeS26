package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

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

    @ParameterizedTest
    @CsvSource({
            "1, '#'",
            "2, '##'",
            "3, '###'"
    })
    void testHeadingPrefixForHeadingLevels(int level, String expectedPrefix) {
        assertEquals(expectedPrefix, parser.getHeadingPrefix(level));
    }

    @ParameterizedTest
    @CsvSource({
            "1, ''",
            "2, '-->'",
            "3, '---->'"
    })
    void testIndentationDashes(int depth, String expectedIndentation) {
        assertEquals(expectedIndentation, parser.getIndentation(depth));
    }

    @Test
    void testBrokenLinkOutputFormattingAtRootDepth(){
        parser.addBrokenLinkToReport("http://error.com", 1);
        assertTrue(testOut.toString().contains("<br>broken link"),"Der kaputte Link auf Tiefe 1 wurde falsch formatiert.");
    }

    @Test
    void testBrokenLinkOutputFormattingAtLowerDepth() {
        parser.addBrokenLinkToReport("http://error.com", 2);
        assertTrue(testOut.toString().contains("<br>--> broken link"),"Der kaputte Link auf Tiefe 2 wurde nicht korrekt eingerückt!");
    }

    @Test
    void testCrawlStopsWhenDepthExceeded(){
        parser.crawl("http://test.at", List.of("test.at"), 10); // 10 > 3 (maxDepth)
        assertTrue(testOut.toString().isEmpty(), "Crawler hätte bei Tiefe > maxDepth sofort stoppen müssen");
    }

    @Test
    void testCrawlStopsWhenUrlAlreadyVisited() {
        parser.markUrlAsVisited("http://schon-besucht.at");
        parser.crawl("http://schon-besucht.at", List.of("test.at"), 1);
        assertTrue(testOut.toString().isEmpty(), "Crawler hätte bei bereits besuchter URL sofort stoppen müssen");
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
    void testCrawlFormatsHeadingsCorrectly() throws IOException {
        String url = "http://mock-test.at";

        // Simuliere die Rückgabe von Jsoup im sauberen String-Format: "tag:text"
        when(mockExtractor.extractHeadings(url)).thenReturn(List.of("h1:Titel"));
        when(mockExtractor.extractLinks(url)).thenReturn(List.of("http://mock-test.at/page2"));

        parser.crawl(url, List.of("mock-test.at"), 1);

        String output = testOut.toString();
        assertTrue(output.contains("# Titel"), "Die Überschrift wurde nicht korrekt formatiert gedruckt!");
    }

    @Test
    void testCrawlMarksUrlAsVisited() throws IOException{
        String url = "http://mock-test.at";

        when(mockExtractor.extractHeadings(url)).thenReturn(List.of());
        when(mockExtractor.extractLinks(url)).thenReturn(List.of());

        parser.crawl(url, List.of("mock-test.at"), 1);
        assertTrue(parser.isUrlVisited(url), "Die Ausgangs-URL sollte im HashSet als besucht markiert sein.");
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