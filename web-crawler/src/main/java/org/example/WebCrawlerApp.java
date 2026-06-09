package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger logger = LoggerFactory.getLogger(WebCrawlerApp.class);
    private static final int THREAD_POOL_SIZE = 8;
    private static final int AWAIT_TERMINATION_MINUTES = 2;

    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Gebrauch: java WebCrawlerApp <URL> <Tiefe> <Domain> <Domain> ... <Domain>");
            return;
        }

        String startUrl = args[0];
        int maxDepth = parseDepth(args[1]);
        if (maxDepth == -1) return;

        List<String> domains = Arrays.asList(Arrays.copyOfRange(args, 2, args.length));

        try {
            WebCrawlerApp crawler = new WebCrawlerApp();
            crawler.startCrawling(startUrl, maxDepth, domains);
        } catch (Exception e) {
            logger.error("Unerwarteter Fehler im Hauptprozess: ", e);
        }
    }

    public void startCrawling(String startUrl, int maxDepth, List<String> domains) {
        List<PageReport> allReports = Collections.synchronizedList(new ArrayList<>());
        Set<String> visitedURLs = Collections.synchronizedSet(new HashSet<>());
        List<String> urlsToCrawlNext = new ArrayList<>(List.of(startUrl));

        visitedURLs.add(startUrl);
        HtmlDataExtractor extractor = new JsoupDataExtractor();

        logger.info("CRAWLER START");

        for (int currentDepth = 1; currentDepth <= maxDepth; currentDepth++) {
            if (urlsToCrawlNext.isEmpty()) {
                break;
            }

            logger.info("Starte Crawl-Ebene: {}", currentDepth);

            runLevelCrawling(urlsToCrawlNext, currentDepth, domains, extractor, allReports);

            urlsToCrawlNext = collectNextLevelUrls(allReports, currentDepth, visitedURLs);
        }

        generateFinalReport(allReports);
    }

    private void runLevelCrawling(List<String> urls, int depth, List<String> domains, HtmlDataExtractor extractor, List<PageReport> reports) {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        for (String url : urls) {
            executor.execute(new HtmlParser(url, depth, domains, extractor, reports));
        }

        executor.shutdown();
        waitForThreads(executor);
    }

    private List<String> collectNextLevelUrls(List<PageReport> allReports, int currentDepth, Set<String> visitedURLs) {
        List<String> nextLevelUrls = new ArrayList<>();

        synchronized (allReports) {
            for (PageReport report : allReports) {
                if (report.getDepth() == currentDepth) {
                    filterAndAddNewUrls(report.getLinks(), visitedURLs, nextLevelUrls);
                }
            }
        }
        return nextLevelUrls;
    }

    private void filterAndAddNewUrls(List<String> extractedLinks, Set<String> visitedURLs, List<String> nextLevelUrls) {
        for (String extractedUrl : extractedLinks) {
            if (visitedURLs.add(extractedUrl)) {
                nextLevelUrls.add(extractedUrl);
            }
        }
    }

    private void generateFinalReport(List<PageReport> allReports) {
        allReports.sort(java.util.Comparator
                .comparingInt(PageReport::getDepth)
                .thenComparing(PageReport::getUrl)
        );

        ReportWriter writer = new ReportWriter();
        writer.writeReport("report.md", allReports);

        logger.info("CRAWLER ENDE -> Bericht erfolgreich generiert.");
    }

    private static int parseDepth(String depthArg) {
        try {
            return Integer.parseInt(depthArg);
        } catch (NumberFormatException e) {
            logger.error("Fehler: Das zweite Argument (Tiefe) muss eine gültige Ganzzahl sein.");
            return -1;
        }
    }

    private void waitForThreads(ExecutorService executor) {
        try {
            if (!executor.awaitTermination(AWAIT_TERMINATION_MINUTES, TimeUnit.MINUTES)) {
                logger.warn("Timeout erreicht! Einige Threads wurden vorzeitig abgebrochen.");
            }
        } catch (InterruptedException e) {
            logger.error("Fehler beim Warten auf die Threads: {}", e.getMessage());
            Thread.currentThread().interrupt();
        }
    }
}