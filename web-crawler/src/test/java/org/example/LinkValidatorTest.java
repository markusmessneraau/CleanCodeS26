package org.example;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LinkValidatorTest {

    @Test
    void shouldReturnFalseIfUrlIsNull() {
        String url = null;
        boolean result = LinkValidator.isValid(url, "aau.at");
        assertFalse(result);
    }

    @Test
    void shouldReturnTrueIfUrlIsValidAndContainsAllowedDomain() {
        String url = "https://www.aau.at/";
        boolean result = LinkValidator.isValid(url, "aau.at");
        assertTrue(result);
    }

    @Test
    void shouldReturnFalseIfUrlIsValidButIsOutsideAllowedDomain() {
        String url = "https://www.aau.at/";
        boolean result = LinkValidator.isValid(url, "google.com");
        assertFalse(result);
    }

    @Test
    void shouldReturnFalseIfUrlIsEmpty() {
        String url = "";
        boolean result = LinkValidator.isValid(url, "aau.at");
        assertFalse(result);
    }

    @Test
    void shouldReturnFalseIfProtocolIsInvalid() {
        assertFalse(LinkValidator.isValid("mailto:info@aau.at", "aau.at"));
    }

    @Test
    void shouldReturnFalseIfUrlHasSpaces(){
        assertFalse(LinkValidator.isValid("https://www .aau.at/", "aau.at" ));
    }

}
