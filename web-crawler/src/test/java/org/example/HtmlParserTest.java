package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HtmlParserTest {
    private HtmlParser parser;
    private HtmlDataExtractor mockExtractor; // Unser neuer Mock für das Interface

    @BeforeEach
    void setUp() {
        mockExtractor = mock(HtmlDataExtractor.class); // Interface mocken statt Jsoup!
        // Dem Parser übergeben wir jetzt den Mock
        parser = new HtmlParser(3, mockExtractor);
    }

    @Test
    void testCrawlStopsWhenDepthExceeded(){
        parser.crawl("http://test.at", List.of("test.at"), 10); // 10 > 3 (maxDepth)
        assertTrue(parser.getAllReports().isEmpty(), "Crawler hätte bei Tiefe > maxDepth sofort stoppen müssen");
    }

    @Test
    void testCrawlStopsWhenUrlAlreadyVisited() {
        parser.markUrlAsVisited("http://schon-besucht.at");
        parser.crawl("http://schon-besucht.at", List.of("test.at"), 1);
        assertTrue(parser.getAllReports().isEmpty(), "Crawler hätte bei bereits besuchter URL sofort stoppen müssen");
    }

    @Test
    void testHandleLinksSkipsInvalid() throws IOException {
        String url = "http://meine-seite.at";

        // Wir sagen dem Mock einfach, was er zurückgeben soll, wenn er gefragt wird
        when(mockExtractor.extractHeadings(url)).thenReturn(List.of());
        when(mockExtractor.extractLinks(url)).thenReturn(List.of("http://google.com"));

        parser.crawl(url, List.of("meine-seite.at"), 1);

        PageReport report = parser.getAllReports().getFirst();

        assertFalse(report.getLinks().contains("google.com"), "Ungültiger Link sollte ignoriert werden");
    }

    @Test
    void testCrawlSavesHeadingsCorrectly() throws IOException {
        String url = "http://mock-test.at";

        // Simuliert die Rückgabe des Extractors
        when(mockExtractor.extractHeadings(url)).thenReturn(List.of("h1:Titel"));
        when(mockExtractor.extractLinks(url)).thenReturn(List.of());

        parser.crawl(url, List.of("mock-test.at"), 1);

        List<PageReport> reports = parser.getAllReports();
        assertEquals(1, reports.size());
        assertTrue(reports.getFirst().getHeadings().contains("h1:Titel"), "Die Überschrift wurde nicht im Report gespeichert.");
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
    void testCrawlThrowsExceptionMarksAsBroken() throws IOException {
        String url = "http://kaputt.at";
        List<String> domains = List.of("kaputt.at");

        // Wenn das Interface aufgerufen wird, lassen wir es eine IOException werfen
        when(mockExtractor.extractHeadings(url)).thenThrow(new IOException("Simulierter Netzwerkfehler"));

        parser.crawl(url, domains, 1);

        List<PageReport> reports = parser.getAllReports();
        assertEquals(1, reports.size());
        assertTrue(reports.getFirst().isBroken(), "Der Crawler sollte als broken markiert werden.");
    }

    @Test
    void testCrawlSavesValidLinksToReport() throws IOException {
        String url = "http://mock-test.at";
        String folgeUrl = "http://mock-test.at/page2";

        when(mockExtractor.extractHeadings(url)).thenReturn(List.of());
        when(mockExtractor.extractLinks(url)).thenReturn(List.of(folgeUrl));

        when(mockExtractor.extractHeadings(folgeUrl)).thenReturn(List.of());
        when(mockExtractor.extractLinks(folgeUrl)).thenReturn(List.of());

        parser.crawl(url, List.of("mock-test.at"), 1);

        PageReport report = parser.getAllReports().getFirst();
        assertTrue(report.getLinks().contains(folgeUrl), "Der gültige Link hätte im Report gespeichert werden müssen.");
    }
}