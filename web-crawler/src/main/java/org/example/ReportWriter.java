package org.example;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.List;

public class ReportWriter {
    private final ReportFormatter formatter = new ReportFormatter();

    public void writeReport(String outputFilename, List<PageReport> reports) {
        if (reports.isEmpty()) return;
        try (PrintStream fileOut = new PrintStream(new FileOutputStream(outputFilename))) {

            PageReport firstReport = reports.getFirst();
            fileOut.println(formatter.formatInputUrl(firstReport.getUrl()));

            for (PageReport report : reports) {
                fileOut.println(formatter.formatDepthIndicator(report.getDepth()));

                if (report.isBroken()) {
                    fileOut.println(formatter.formatBrokenLink(report.getUrl(), report.getDepth()));
                } else {
                    for (String heading : report.getHeadings()) {
                        String formattedHeading = formatter.formatHeading(heading, report.getDepth());

                        if (!formattedHeading.isEmpty()) {
                            fileOut.println(formattedHeading);
                        }
                    }

                    for (String link : report.getLinks()) {
                        fileOut.println(formatter.formatLink(link, report.getDepth() + 1));
                    }

                }
                fileOut.println();
            }
            System.err.println("Bericht wurde in " + outputFilename + " gespeichert.");
        } catch (FileNotFoundException e){
            System.err.println("Fehler: Datei konnte nicht erstellt werden: " + e.getMessage());
        }

    }
}
