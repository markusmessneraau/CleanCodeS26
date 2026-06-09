package org.example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class WebCrawlerApp {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(WebCrawlerApp.class);

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
            logger.error("Fehler: Das zweite Argument (Tiefe) muss eine gültige Ganzzahl sein.");
            return;
        }

        List<String> domains = Arrays.asList(Arrays.copyOfRange(args, 2, args.length));

        try {
            // THREAD-SAFE: Synchronisierte Collection, um Daten sicher über 8 parallele Threads hinweg zu verarbeiten
            List<PageReport> allReports = Collections.synchronizedList(new ArrayList<>());
            Set<String> visitedURLs = Collections.synchronizedSet(new HashSet<>());

            logger.info("CRAWLER START");

            // Warteschlange für die URLs, die für die aktuell aktive crawl-Ebene geplant sind
            List<String> urlsToCrawlNext = new ArrayList<>();
            urlsToCrawlNext.add(startUrl);
            visitedURLs.add(startUrl);

            HtmlDataExtractor extractor = new JsoupDataExtractor();

            // Breitensuche-Crawl wird Ebene für Ebene ausgeführt
            for (int currentDepth = 1; currentDepth <= maxDepth; currentDepth++) {
                if (urlsToCrawlNext.isEmpty()) {
                    break;
                }

                logger.info("Starte Crawl-Ebene: {}", currentDepth);


                ExecutorService executor = Executors.newFixedThreadPool(8);

                // URLs der aktuellen Ebene als eigenständige Aufgaben in den ThreadPool übergeben
                for (String url : urlsToCrawlNext) {
                    executor.execute(new HtmlParser(url, currentDepth, domains, extractor, allReports));
                }

                // Dem Pool mitteilen, dass für diese spezifische Ebene keine weiteren Aufgaben hinzugefügt werden
                executor.shutdown();
                try {

                    boolean finishedQuietly = executor.awaitTermination(2, TimeUnit.MINUTES);


                    if (!finishedQuietly) {
                        logger.warn("Timeout erreicht! Einige Threads wurden vorzeitig abgebrochen.");
                    }
                } catch (InterruptedException e) {
                    logger.error("Fehler beim Warten auf die Threads: {}", e.getMessage());
                    Thread.currentThread().interrupt(); // Best Practice: Thread-Status wiederherstellen
                }

                // Gültige, neue inks sammeln, die Liste für die nächste Ebene zusammenzustellen
                List<String> nextLevelUrls = new ArrayList<>();
                synchronized (allReports) {
                    for (PageReport report : allReports) {
                        if (report.getDepth() == currentDepth) {
                            for (String extractedUrl : report.getLinks()) {
                                // .add() gibt false zurück, wenn die URL bereits im Set existiert (verhindert Duplikate)
                                if (visitedURLs.add(extractedUrl)) {
                                    nextLevelUrls.add(extractedUrl);
                                }
                            }
                        }
                    }
                }

                // Die Warteschlange für die nächste Ausführungsebene aktualisieren
                urlsToCrawlNext = nextLevelUrls;
            }

            //Ergebnisse zuerst nach Tiefe sortieren, dann alphabetisch nach URL
            allReports.sort(java.util.Comparator
                    .comparingInt(PageReport::getDepth)
                    .thenComparing(PageReport::getUrl)
            );

            // Den sortierten Bericht mit der ReportWriter schreiben
            ReportWriter writer = new ReportWriter();
            writer.writeReport("report.md", allReports);

            logger.info("CRAWLER ENDE -> Bericht erfolgreich generiert.");

        } catch (Exception e) {
            logger.error("Unerwarteter Fehler im Hauptprozess: ", e);
        }
    }
}