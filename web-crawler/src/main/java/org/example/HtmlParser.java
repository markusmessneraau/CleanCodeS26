package org.example;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;

public class HtmlParser {

    private final int maxDepth;
    private final HtmlDataExtractor dataExtractor;
    private final HashSet<String> visitedURLs = new HashSet<>();
    private final List<PageReport> allReports = new ArrayList<>();

    //dataExtractor im Konstruktor hinzugefügt
    public HtmlParser(int maxDepth, HtmlDataExtractor dataExtractor) {
        this.maxDepth = maxDepth;
        this.dataExtractor = dataExtractor;
    }

    public void crawl(String url, List<String> domains, int depth) {
        if (depth > maxDepth || visitedURLs.contains(url)) {
            return;
        }

        visitedURLs.add(url);
        PageReport report = new PageReport(url, depth);
        allReports.add(report);

        try {
            List<String> headings = dataExtractor.extractHeadings(url);
            for (String heading : headings){
                report.addHeading(heading);
            }

            List<String> links = dataExtractor.extractLinks(url);
            for (String link : links) {
                if (LinkValidator.isValid(link, domains)) {
                    report.addLink(link);
                }
            }
            handleLinks(links, domains, depth);
        } catch (IOException e) {
            report.setBroken(true);
        }
    }

    private void handleLinks(List<String> links, List<String> domains, int depth) {
        for (String extractedUrl : links) {
            if (LinkValidator.isValid(extractedUrl, domains)) {
                crawl(extractedUrl, domains, depth + 1);
            }
        }
    }

    public List<PageReport> getAllReports() {
        return allReports;
    }
    public void markUrlAsVisited(String url){
        this.visitedURLs.add(url);
    }

    public boolean isUrlVisited(String url){
        return this.visitedURLs.contains(url);
    }
}