package org.example;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HtmlParserTest {
    private HtmlParser parser;
    private ByteArrayOutputStream testOut;

    @BeforeEach
    void setUp() {
        testOut = new ByteArrayOutputStream();
        parser = new HtmlParser(3, new PrintStream(testOut));
    }



    @Test
    void testHashtagsForHeadingLevels() {
        assertEquals("#", parser.getHashtagsForOutput(1));
        assertEquals("###", parser.getHashtagsForOutput(3));
    }

    @Test
    void testIndentationDashes() {
        assertEquals("", parser.getDashesForOutput(1));
        assertEquals("-->", parser.getDashesForOutput(2));
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

        parser.crawl("http://test.at", "test.at", 10); // 10 > 3 (maxDepth)

        parser.visitedURLs.add("http://schon-besucht.at");
        parser.crawl("http://schon-besucht.at", "test.at", 1);

        assertTrue(testOut.toString().contains("depth: 10") || testOut.toString().isEmpty());
    }

    @Test
    void testHandleLinksSkipsInvalid() throws IOException {
        String url = "http://meine-seite.at";

        String html = "<html><body><a href='http://google.com'>Externer Link</a></body></html>";
        Document doc = Jsoup.parse(html, url);

        try (MockedStatic<Jsoup> mockedJsoup = mockStatic(Jsoup.class)) {
            Connection mockConnection = mock(Connection.class);
            mockedJsoup.when(() -> Jsoup.connect(anyString())).thenReturn(mockConnection);
            when(mockConnection.get()).thenReturn(doc);

            parser.crawl(url, "meine-seite.at", 1);

            assertFalse(testOut.toString().contains("google.com"), "Ungültiger Link sollte ignoriert werden");
        }
    }



    @Test
    void testCrawlSuccessWithMock() throws IOException {
        String url = "http://mock-test.at";

        String html = "<html><body><h1>Titel</h1><a href='http://mock-test.at/page2'>Link</a></body></html>";
        Document realDoc = Jsoup.parse(html, url);

        try (MockedStatic<Jsoup> mockedJsoup = mockStatic(Jsoup.class)) {
            Connection mockConnection = mock(Connection.class);

            mockedJsoup.when(() -> Jsoup.connect(anyString())).thenReturn(mockConnection);
            when(mockConnection.get()).thenReturn(realDoc);

            parser.crawl(url, "mock-test.at", 1);

            String output = testOut.toString();

            // Jetzt kannst du prüfen, ob er wirklich "drin" war:
            assertTrue(output.contains("# Titel"), "Die Überschrift wurde nicht gedruckt!");
            assertTrue(parser.visitedURLs.contains(url), "URL sollte im HashSet sein");
        }
    }

    @Test
    void testCrawlThrowsException() throws IOException {
        String url = "http://kaputt.at";

        try (MockedStatic<Jsoup> mockedJsoup = mockStatic(Jsoup.class)) {
            Connection mockConnection = mock(Connection.class);
            mockedJsoup.when(() -> Jsoup.connect(url)).thenReturn(mockConnection);


            when(mockConnection.get()).thenThrow(new IOException("Netzwerkfehler"));

            parser.crawl(url, "kaputt.at", 1);
            assertTrue(testOut.toString().contains("broken link"), "Fehler sollte im Report stehen");
        }
    }
}