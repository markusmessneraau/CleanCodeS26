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


        HtmlParser parser = new HtmlParser(maxDepth);

        try {
            // Leitet alle Ausgaben (System.out.println) in die Datei um
            PrintStream fileOut = new PrintStream(new FileOutputStream("report.md"));
            System.setOut(fileOut);

            System.out.println("CRAWLER START");

            parser.crawl(startUrl, domain, 1);
            System.out.println("CRAWLER END");

            fileOut.close();


            System.err.println("Bericht wurde in report.md gespeichert.");

        } catch (Exception e) {
            System.err.println("Fehler: " + e.getMessage());
        }
    }
}