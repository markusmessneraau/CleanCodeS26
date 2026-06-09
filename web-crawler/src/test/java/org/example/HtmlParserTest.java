package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HtmlParserTest {

    private HtmlDataExtractor mockExtractor;
    private List<PageReport> testReports;
    private List<String> allowedDomains;

    @BeforeEach
    void setUp() {

        mockExtractor = mock(HtmlDataExtractor.class);
        testReports = Collections.synchronizedList(new ArrayList<>());

        allowedDomains = List.of("mock-test.at", "meine-seite.at");
    }

    @Test
    void testCrawlSavesHeadingsCorrectly() throws IOException {
        String url = "http://mock-test.at";

        when(mockExtractor.extractHeadings(url)).thenReturn(List.of("h1:Titel"));
        when(mockExtractor.extractLinks(url)).thenReturn(List.of());

        HtmlParser parser = new HtmlParser(url, 1, allowedDomains, mockExtractor, testReports);

        parser.run();

        assertEquals(1, testReports.size());
        PageReport report = testReports.getFirst();
        assertEquals(url, report.getUrl());
        assertEquals(1, report.getDepth());
        assertTrue(report.getHeadings().contains("h1:Titel"), "Die Überschrift wurde nicht im Report gespeichert.");
    }

    @Test
    void testHandleLinksSkipsInvalid() throws IOException {
        String url = "http://meine-seite.at";

        when(mockExtractor.extractHeadings(url)).thenReturn(List.of());
        when(mockExtractor.extractLinks(url)).thenReturn(List.of("http://google.com"));

        HtmlParser parser = new HtmlParser(url, 1, allowedDomains, mockExtractor, testReports);
        parser.run();

        assertEquals(1, testReports.size());
        PageReport report = testReports.getFirst();
        assertFalse(report.getLinks().contains("http://google.com"), "Ungültiger Link sollte ignoriert werden");
    }

    @Test
    void testCrawlSavesValidLinksToReport() throws IOException {
        String url = "http://mock-test.at";
        String folgeUrl = "http://mock-test.at/page2";

        when(mockExtractor.extractHeadings(url)).thenReturn(List.of());
        when(mockExtractor.extractLinks(url)).thenReturn(List.of(folgeUrl));

        HtmlParser parser = new HtmlParser(url, 1, allowedDomains, mockExtractor, testReports);
        parser.run();

        assertEquals(1, testReports.size());
        PageReport report = testReports.getFirst();
        assertTrue(report.getLinks().contains(folgeUrl), "Der gültige Link hätte im Report gespeichert werden müssen.");
    }

    @Test
    void testCrawlThrowsExceptionMarksAsBroken() throws IOException {
        String url = "http://kaputt.at";


        when(mockExtractor.extractHeadings(url)).thenThrow(new IOException("Simulierter Netzwerkfehler"));

        HtmlParser parser = new HtmlParser(url, 1, List.of("kaputt.at"), mockExtractor, testReports);
        parser.run();


        assertEquals(1, testReports.size());
        PageReport report = testReports.getFirst();

        assertTrue(report.isBroken(), "Der Report sollte als broken markiert werden.");
    }
}