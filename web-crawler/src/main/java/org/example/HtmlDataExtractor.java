package org.example;

import java.io.IOException;
import java.util.List;

public interface HtmlDataExtractor {
    List<String> extractHeadings(String url) throws IOException;
    List<String> extractLinks(String url) throws IOException;
}