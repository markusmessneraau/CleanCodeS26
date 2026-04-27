package org.example;

import java.io.FileOutputStream;
import java.io.PrintStream;

public class CrawlDemo {
    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Gebrauch: java CrawlDemo <URL> <Tiefe> <Domain>");
            return;
        }

        String startUrl = args[0];
        int maxDepth = Integer.parseInt(args[1]);
        String domain = args[2];

        try (PrintStream fileOut = new PrintStream(new FileOutputStream("report.md"))) {

            HtmlParser parser = new HtmlParser(maxDepth, fileOut);

            fileOut.println("CRAWLER START");
            parser.crawl(startUrl, domain, 1);
            fileOut.println("CRAWLER END");

            System.err.println("Bericht wurde in report.md gespeichert.");

        } catch (Exception e) {
            System.err.println("Fehler: " + e.getMessage());
        }
    }
}