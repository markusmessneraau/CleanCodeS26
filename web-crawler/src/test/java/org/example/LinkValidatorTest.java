package org.example;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LinkValidatorTest {

    @Test
    void shouldReturnFalseIfUrlIsNull() {
        String url = null;
        boolean result = LinkValidator.isValid(url, List.of("aau.at"));
        assertFalse(result);
    }

    @Test
    void shouldReturnTrueIfUrlIsValidAndContainsAllowedDomain() {
        String url = "https://www.aau.at/";
        boolean result = LinkValidator.isValid(url, List.of("aau.at"));
        assertTrue(result);
    }

    @Test
    void shouldReturnFalseIfUrlIsValidButIsOutsideAllowedDomain() {
        String url = "https://www.aau.at/";
        boolean result = LinkValidator.isValid(url, List.of("google.com"));
        assertFalse(result);
    }

    @Test
    void shouldReturnFalseIfUrlIsEmpty() {
        String url = "";
        boolean result = LinkValidator.isValid(url, List.of("aau.at"));
        assertFalse(result);
    }

    @Test
    void shouldReturnFalseIfProtocolIsInvalid() {
        assertFalse(LinkValidator.isValid("mailto:info@aau.at", List.of("aau.at")));
    }

    @Test
    void shouldReturnTrueForSubdomains() {
        assertTrue(LinkValidator.isValid("https://campus.aau.at", List.of("aau.at")));
    }

    @Test
    void shouldReturnFalseIfNoDomainMatches() {
        assertFalse(LinkValidator.isValid("https://facebook.com", List.of("aau.at")));
    }

    @Test
    void shouldHitNullHostCheck() {
        assertFalse(LinkValidator.isValid("http://:80", List.of("aau.at")));
    }

    @Test
    void shouldHitCatchBlock() {
        assertFalse(LinkValidator.isValid("http://[", List.of("aau.at")));
    }

}
