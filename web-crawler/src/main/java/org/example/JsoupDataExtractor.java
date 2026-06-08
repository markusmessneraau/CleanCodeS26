package org.example;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JsoupDataExtractor implements HtmlDataExtractor {

    private static final int TIMEOUT_MS = 5000;

    @Override
    public List<String> extractHeadings(String url) throws IOException {
        Document doc = Jsoup.connect(url).timeout(TIMEOUT_MS).get();
        Elements headings = doc.select("h1, h2, h3, h4, h5, h6");

        List<String> headingTexts = new ArrayList<>();
        for (Element heading : headings) {
            headingTexts.add(heading.tagName() + ":" + heading.text());
        }
        return headingTexts;
    }

    @Override
    public List<String> extractLinks(String url) throws IOException {
        Document doc = Jsoup.connect(url).timeout(TIMEOUT_MS).get();
        Elements links = doc.select("a[href]");

        List<String> absoluteUrls = new ArrayList<>();
        for (Element link : links) {
            String absUrl = link.absUrl("href");
            if (absUrl != null && !absUrl.isEmpty()) {
                absoluteUrls.add(absUrl);
            }
        }
        return absoluteUrls;
    }
}