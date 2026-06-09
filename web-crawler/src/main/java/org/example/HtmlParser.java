package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import org.jsoup.HttpStatusException;
import java.util.List;

public class HtmlParser implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(HtmlParser.class);

    private final String url;
    private final int depth;
    private final List<String> domains;
    private final HtmlDataExtractor dataExtractor;
    private final List<PageReport> allReports;

    public HtmlParser(String url, int depth, List<String> domains, HtmlDataExtractor dataExtractor, List<PageReport> allReports) {
        this.url = url;
        this.depth = depth;
        this.domains = domains;
        this.dataExtractor = dataExtractor;
        this.allReports = allReports;
    }

    @Override
    public void run() {
        logger.info("startet Crawl für: {}", url);
        crawl();
    }


    public void crawl() {
        PageReport report = new PageReport(url, depth);

        try {
            extractPageData(report);
        } catch (Exception e) {

            handleCrawlException(e, report);
        } finally {
            saveReport(report);
        }
    }


    private void extractPageData(PageReport report) throws IOException {
        List<String> headings = dataExtractor.extractHeadings(url);
        for (String heading : headings) {
            report.addHeading(heading);
        }

        List<String> links = dataExtractor.extractLinks(url);
        for (String link : links) {
            if (LinkValidator.isValid(link, domains)) {
                report.addLink(link);
            }
        }
    }


    private void handleCrawlException(Exception e, PageReport report) {

        report.setBroken(true);

        switch (e) {
            case MalformedURLException malformedURLException ->
                    logger.error("FEHLER: Die Webadresse [{}] ist ungültig oder falsch geschrieben.", url);

            case HttpStatusException httpException ->
                    logHttpStatusError(httpException);

            case UnknownHostException unknownHostException ->
                    logger.error("FEHLER: Die Webseite [{}] ist nicht erreichbar. (Prüfen Sie die Internetverbindung oder die Domain)", url);

            case SocketTimeoutException socketTimeoutException ->
                    logger.error("TIMEOUT: Die Webseite [{}] hat nicht rechtzeitig geantwortet (Zeitüberschreitung).", url);

            case IOException ioException ->
                    logger.error("NETZWERKFEHLER: Verbindung zur Webseite [{}] fehlgeschlagen.", url);

            case null, default ->
                    logger.error("SYSTEMFEHLER: Ein unerwarteter Programmfehler ist bei [{}] aufgetreten: ", url, e);
        }
    }


    private void logHttpStatusError(HttpStatusException e) {
        if (e.getStatusCode() == 404) {
            logger.error("HTTP 404 - Die gesuchte Seite existiert nicht: {}", url);
        } else if (e.getStatusCode() == 403) {
            logger.error("HTTP 403 - Zugriff verweigert. Der Server blockiert bei: {}", url);
        } else {
            logger.error("HTTP {} - Der Server meldet ein Problem bei: {}", e.getStatusCode(), url);
        }
    }


    private void saveReport(PageReport report) {
        synchronized (allReports) {
            allReports.add(report);
        }
    }
}