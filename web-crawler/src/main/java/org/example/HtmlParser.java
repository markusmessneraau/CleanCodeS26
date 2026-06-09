package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
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

        } catch (IOException e) {
            logger.error("Fehler bei {}: {}", url, e.getMessage());
            report.setBroken(true);
        } finally {
            synchronized (allReports) {
                allReports.add(report);
            }
        }
    }
}