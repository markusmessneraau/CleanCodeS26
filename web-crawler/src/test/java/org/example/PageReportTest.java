package org.example;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PageReportTest {

    @Test
    void testShouldInitializeWithDefaultValues() {
        PageReport report = new PageReport("http://test.at", 2);

        assertAll("Initialer Zustand des PageReport",
                () -> assertEquals("http://test.at", report.getUrl()),
                () -> assertEquals(2, report.getDepth()),
                () -> assertFalse(report.isBroken(), "Ein neuer Report sollte standardmäßig nicht kaputt sein."),
                () -> assertTrue(report.getHeadings().isEmpty(), "Die Headings-Liste muss am Anfang leer sein."),
                () -> assertTrue(report.getLinks().isEmpty(), "Die Links-Liste muss am Anfang leer sein.")
        );
    }

    @Test
    void testAddHeadingStoresHeadingCorrectly() {
        PageReport report = new PageReport("http://test.at", 1);
        report.addHeading("h1:Titel");

        assertEquals(1, report.getHeadings().size());
        assertTrue(report.getHeadings().contains("h1:Titel"));
    }

    @Test
    void testAddLinkStoresLinkCorrectly() {
        PageReport report = new PageReport("http://test.at", 1);
        report.addLink("http://next-page.com");

        assertEquals(1, report.getLinks().size());
        assertTrue(report.getLinks().contains("http://next-page.com"));
    }

    @Test
    void testSetBrokenUpdatesStatus() {
        PageReport report = new PageReport("http://test.at", 1);
        report.setBroken(true);

        assertTrue(report.isBroken());
    }

}
