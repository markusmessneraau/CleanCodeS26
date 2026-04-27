package org.example;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


import java.io.IOException;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.List;

public class HtmlParser {

    private final int maxDepth;
    private final PrintStream out;
    HashSet<String> visitedURLs = new HashSet<>();

    public HtmlParser(int maxDepth, PrintStream out) {
        this.maxDepth = maxDepth;
        this.out = out;
    }

    public void crawl(String url, List<String> domains, int depth) {
        if (depth > maxDepth || visitedURLs.contains(url)) {
            return;
        }

        visitedURLs.add(url);
        addMetaDataToReport(url, depth);

        try {
            Document website = Jsoup.connect(url).get();
            addHeadingsToReport(website, depth);
            handleLinks(website, domains, depth);
        } catch (IOException e) {
            addBrokenLinkToReport(url, depth);
            System.err.println("Debug: " + url + " -> " + e.getMessage());
        }
    }

    private void addHeadingsToReport(Document website, int depth) {
        Elements headings = website.select("h1, h2, h3, h4, h5, h6");
        String dashes = getIndentation(depth);

        for (Element heading : headings) {
            String tagOfHeading = heading.tagName();
            int levelOfHeading = Character.getNumericValue(tagOfHeading.charAt(1));
            String hashtags = getHashtagPrefix(levelOfHeading);

            out.println(hashtags + " " + dashes + heading.text());
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

    private void handleLinks(Document website, List<String> domains, int depth) {
        Elements links = website.select("a[href]");
        for (Element link : links) {
            String extractedUrl = link.absUrl("href");
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
