package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;


import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ReportWriterTest {

    private ReportWriter writer;
    private Path tempFile;

    @BeforeEach
    void setUp(@TempDir Path tempDir) {
        writer = new ReportWriter();
        tempFile = tempDir.resolve("test_report.md");
    }

    @Test
    void shouldCreateFileAndWriteContent() throws Exception {
        PageReport sampleReport = new PageReport("http://test-seite.at", 1);
        sampleReport.addHeading("h1:Willkommen");
        sampleReport.addLink("http://folgelink.at");

        writer.writeReport(tempFile.toString(), List.of(sampleReport));

        assertTrue(Files.exists(tempFile), "Die Report-Datei wurde nicht erstellt!");

        String content = Files.readString(tempFile);
        assertTrue(content.contains("http://test-seite.at"), "Die Start-URL fehlt in der Datei.");
        assertTrue(content.contains("Willkommen"), "Die Überschrift wurde nicht reingeschrieben.");
    }

    @Test
    void shouldNotCreateFileIfReportListIsEmpty() {
        writer.writeReport(tempFile.toString(), List.of());

        assertFalse(Files.exists(tempFile), "Bei einer leeren Liste sollte keine Datei erstellt werden.");
    }

    @Test
    void shouldWriteBrokenLinkMessageIfLinkIsBroken() throws Exception {
        PageReport brokenReport = new PageReport("http://broken-link.at", 1);
        brokenReport.setBroken(true);

        writer.writeReport(tempFile.toString(), List.of(brokenReport));

        assertTrue(Files.exists(tempFile));
        assertTrue(Files.readString(tempFile).contains("broken"), "Der kaputte Link wurde nicht in die Datei geschrieben!");
    }
}
