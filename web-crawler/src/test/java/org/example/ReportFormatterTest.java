package org.example;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReportFormatterTest {
    private final ReportFormatter formatter = new ReportFormatter();

    @ParameterizedTest
    @CsvSource({
            "1, '#'",
            "2, '##'",
            "3, '###'"
    })
    void testHeadingPrefixForHeadingLevels(int level, String expectedPrefix) {
        assertEquals(expectedPrefix, formatter.getHeadingPrefix(level));
    }

    @ParameterizedTest
    @CsvSource({
            "1, ''",
            "2, '-->'",
            "3, '---->'"
    })
    void testIndentationDashes(int depth, String expectedIndentation) {
        assertEquals(expectedIndentation, formatter.getIndentation(depth));
    }

    @Test
    void testBrokenLinkOutputFormattingAtRootDepth() {
        String result = formatter.formatBrokenLink("http://error.com", 1);
        assertTrue(result.contains("broken link"), "Der kaputte Link auf Tiefe 1 wurde falsch formatiert.");
    }

    @Test
    void testBrokenLinkOutputFormattingAtLowerDepth() {
        String result = formatter.formatBrokenLink("http://error.com", 2);
        assertTrue(result.contains("<br>--> broken link"), "Der kaputte Link auf Tiefe 2 wurde nicht korrekt eingerückt!");
    }

    @Test
    void testFormatHeading() {
        String result = formatter.formatHeading("h1: MyHeading", 2);
        assertEquals("# --> MyHeading", result);
    }

    @Test
    void testFormatLink() {
        String result = formatter.formatLink("http://test.at", 1);
        assertEquals("<br> link to <a> http://test.at</a>", result);
    }

    @Test
    void testFormatDepthIndicator() {
        String result = formatter.formatDepthIndicator(3);
        assertEquals("<br>depth: 3", result);
    }

    @Test
    void testFormatInputUrl() {
        String result = formatter.formatInputUrl("http://start.at");
        assertEquals("input: <a>http://start.at</a>", result);
    }
}
