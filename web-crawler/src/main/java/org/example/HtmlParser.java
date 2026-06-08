package org.example;

import java.io.IOException;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.List;

public class HtmlParser {

    private final int maxDepth;
    private final PrintStream out;
    private final HtmlDataExtractor dataExtractor;
    HashSet<String> visitedURLs = new HashSet<>();

    //dataExtractor im Konstruktor hinzugefügt
    public HtmlParser(int maxDepth, PrintStream out, HtmlDataExtractor dataExtractor) {
        this.maxDepth = maxDepth;
        this.out = out;
        this.dataExtractor = dataExtractor;
    }

    public void crawl(String url, List<String> domains, int depth) {
        if (depth > maxDepth || visitedURLs.contains(url)) {
            return;
        }

        visitedURLs.add(url);
        addMetaDataToReport(url, depth);

        try {

            List<String> headings = dataExtractor.extractHeadings(url);
            addHeadingsToReport(headings, depth);

            List<String> links = dataExtractor.extractLinks(url);
            handleLinks(links, domains, depth);
        } catch (IOException e) {
            addBrokenLinkToReport(url, depth);
            System.err.println("Debug: " + url + " -> " + e.getMessage());
        }
    }


    private void addHeadingsToReport(List<String> headings, int depth) {
        String dashes = getIndentation(depth);

        for (String heading : headings) {

            String[] parts = heading.split(":", 2);
            if (parts.length < 2) continue;

            String tagOfHeading = parts[0];
            String headingText = parts[1];

            int levelOfHeading = Character.getNumericValue(tagOfHeading.charAt(1));
            String hashtags = getHashtagPrefix(levelOfHeading);

            out.println(hashtags + " " + dashes + headingText);
        }
    }

    String getIndentation(int depth) {
        String dashes = "";
        if (depth > 1) {
            for (int i = 0; i < (depth - 1) * 2; i++) {
                dashes += "-";
            }
            dashes += ">";
        }
        return dashes;
    }

    String getHashtagPrefix(int amount) {
        String hashtags = "";
        for (int i = 0; i < amount; i++) {
            hashtags += "#";
        }
        return hashtags;
    }

    private void handleLinks(List<String> links, List<String> domains, int depth) {
        for (String extractedUrl : links) {
            if (LinkValidator.isValid(extractedUrl, domains)) {
                addLinkToReport(extractedUrl, depth + 1);
                crawl(extractedUrl, domains, depth + 1);
            }
        }
    }

    private void addLinkToReport(String url, int nextDepth) {
        String dashes = getIndentation(nextDepth);
        out.println("<br>" + dashes + " link to <a> " + url + "</a>");
    }

    void addBrokenLinkToReport(String url, int depth) {
        String dashes = getIndentation(depth);
        String prefix = dashes.isEmpty() ? "" : dashes + " ";
        out.println("<br>" + prefix + "broken link <a>" + url + "</a>");
    }

    private void addMetaDataToReport(String url, int depth) {
        if (depth == 1) {
            out.println("input: <a>" + url + "</a>");
        }
        out.println("<br>depth: " + depth);
    }
}