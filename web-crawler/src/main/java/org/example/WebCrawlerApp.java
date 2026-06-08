package org.example;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

public class WebCrawlerApp {
    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Gebrauch: java WebCrawlerApp <URL> <Tiefe> <Domain> <Domain> ... <Domain>");
            return;
        }

        String startUrl = args[0];
        int maxDepth;
        try {
            maxDepth = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            System.err.println("Fehler: Das zweite Argument muss eine gültige Ganzzahl sein.");
            return;
        }

        List<String> domains = Arrays.asList(Arrays.copyOfRange(args, 2, args.length));

        try {
            HtmlDataExtractor extractor = new JsoupDataExtractor();
            HtmlParser parser = new HtmlParser(maxDepth, extractor);
            parser.crawl(startUrl, domains, 1);

            ReportWriter writer = new ReportWriter();
            writer.writeReport("report.md", parser.getAllReports());

        } catch (Exception e) {
            System.err.println("Fehler: " + e.getMessage());
        }
    }
}