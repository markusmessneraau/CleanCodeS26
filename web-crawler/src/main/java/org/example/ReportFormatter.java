package org.example;

public class ReportFormatter {

    String formatHeading(String unformattedHeading, int depth) {
        String[] parts = unformattedHeading.split(":", 2);
        if (parts.length < 2) return "";

        String tagOfHeading = parts[0];
        String headingText = parts[1];

        int levelOfHeading = Character.getNumericValue(tagOfHeading.charAt(1));
        String headingPrefix = getHeadingPrefix(levelOfHeading);
        String dashes = getIndentation(depth);

        return headingPrefix + " " + dashes + headingText;
    }

    String formatLink(String url, int depth) {
        String dashes = getIndentation(depth);
        return "<br>" + dashes + " link to <a> " + url + "</a>";
    }

    String formatBrokenLink(String url, int depth) {
        String dashes = getIndentation(depth);
        String prefix = dashes.isEmpty() ? "" : dashes + " ";
        return "<br>" + prefix + "broken link <a>" + url + "</a>";
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

    String getHeadingPrefix(int amount) {
        String prefix = "";
        for (int i = 0; i < amount; i++) {
            prefix += "#";
        }
        return prefix;
    }

    String formatDepthIndicator(int depth) {
        return "<br>depth: " + depth;
    }

    String formatInputUrl(String url) {
        return "input: <a>" + url + "</a>";
    }
}
