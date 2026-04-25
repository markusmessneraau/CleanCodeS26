package org.example;

public class CrawlDemo {
    public static void main(String[] args) {
        HtmlParser parser = new HtmlParser();
        
        String startUrl = "https://de.wikipedia.org/wiki/Kleinkatzen";
        String domain = "wikipedia.org";

        System.out.println("CRAWLER START");
        parser.crawl(startUrl, domain, 1);
        System.out.println("CRAWLER END");
    }
}