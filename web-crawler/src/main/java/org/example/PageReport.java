package org.example;

import java.util.ArrayList;
import java.util.List;

public class PageReport {
    private final String url;
    private final int depth;
    private final List<String> headings = new ArrayList<>();
    private boolean isBroken = false;

    private final List<String> links = new ArrayList<>();
    public PageReport(String url, int depth) {
        this.url = url;
        this.depth = depth;
    }

    public void addHeading(String heading) {
        this.headings.add(heading);
    }

    public void setBroken(boolean broken) {
        this.isBroken = broken;
    }

    public String getUrl() {
        return url;
    }

    public int getDepth() {
        return depth;
    }

    public List<String> getHeadings() {
        return headings;
    }

    public boolean isBroken() {
        return isBroken;
    }

    public void addLink(String link) {
        this.links.add(link);
    }

    public List<String> getLinks() {
        return links;
    }

}
