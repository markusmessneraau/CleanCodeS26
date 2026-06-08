package org.example;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

public class WebCrawlerApp {
    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Gebrauch: java CrawlDemo <URL> <Tiefe> <Domain> <Domain> ... <Domain>");
            return;
        }

        String startUrl = args[0];
        int maxDepth = Integer.parseInt(args[1]);
        List<String> domains = Arrays.asList(Arrays.copyOfRange(args, 2, args.length));

        try (PrintStream fileOut = new PrintStream(new FileOutputStream("report.md"))) {


            HtmlDataExtractor extractor = new JsoupDataExtractor();
            HtmlParser parser = new HtmlParser(maxDepth, fileOut, extractor);

            System.out.println("CRAWLER START");
            fileOut.println("CRAWLER START");
            parser.crawl(startUrl, domains, 1);
            fileOut.println("CRAWLER END");
            System.out.println("CRAWLER END");

            System.err.println("Bericht wurde in report.md gespeichert.");

        } catch (Exception e) {
            System.err.println("Fehler: " + e.getMessage());
        }
    }
}