package org.example;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


import java.io.IOException;

public class HtmlParser {
    private static final int MAX_DEPTH = 3;

    public void crawl(String url, String domain, int depth) {

        if (depth > MAX_DEPTH) {
            return;
        }
        addMetaDataToReport(url, depth);

        try {
            Document website = Jsoup.connect(url).get();

            printHeadingsofWebSite(website, depth);

            handleLinks(website, domain, depth);


        } catch (IOException e) {
            addBrokenLinkToReport(url, depth);
            System.err.println("Debug: " + url + " -> " + e.getMessage());
        }

    }

    private void printHeadingsofWebSite(Document website, int depth) {

        Elements headings = website.select("h1, h2, h3, h4, h5, h6");

        String dashes = getDashesForOutput(depth);

        for (Element heading : headings) {

            String tagOfHeading = heading.tagName();

            int levelOfHeading = Character.getNumericValue(tagOfHeading.charAt(1));

            String hashtags = getHashtagsForOutput(levelOfHeading);

            System.out.println(hashtags + " " + dashes + heading.text());
        }
    }


    private String getDashesForOutput(int depth) {
        String dashes = "";

        if (depth > 1) {
            for (int i = 0; i < (depth - 1) * 2; i++) {
                dashes += "-";
            }
            dashes += ">";
        }
        return dashes;
    }


    private String getHashtagsForOutput(int levelOfHeading) {
        String hashtags = "";

        for (int j = 0; j < levelOfHeading; j++) {
            hashtags += "#";
        }
        return hashtags;
    }

    private void handleLinks(Document website, String domain, int depth) {

        Elements links = website.select("a[href]");

        for (Element link : links) {
            String extractedUrl = link.absUrl("href");
            if (LinkValidator.isValid(extractedUrl, domain)) {
                addLinkToReport(extractedUrl, depth + 1);
                crawl(extractedUrl, domain, depth + 1);
            }

        }

    }

    private void addLinkToReport(String url, int nextDepth) {
        String dashes = getDashesForOutput(nextDepth);
        System.out.println("<br>" + dashes + " link to <a> " + url + "</a>");
    }

    private void addBrokenLinkToReport(String url, int depth) {
        String dashes = getDashesForOutput(depth);
        String prefix = dashes.isEmpty() ? "" : dashes + " ";
        System.out.println("<br>" + prefix + "broken link <a>" + url + "</a>");
    }

    private void addMetaDataToReport(String url, int depth){
        if (depth == 1) {
            System.out.println("input: <a>" + url + "</a>");
        }
        System.out.println("<br>depth: " + depth);
    }


}
