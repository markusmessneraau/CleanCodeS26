package org.example;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LinkValidatorTest {

    @Test
    void shouldReturnFalseIfUrlIsNull() {
        assertFalse(LinkValidator.isValid(null,"aau.at"));
    }

    @Test
    void shouldReturnTrueIfUrlIsValidAndContainsAllowedDomain() {
        assertTrue(LinkValidator.isValid("https://www.aau.at/", "aau.at"));
    }

    @Test
    void shouldReturnFalseIfUrlIsValidButIsOutsideAllowedDomain() {
        assertFalse(LinkValidator.isValid("https://www.aau.at/", "google.com"));
    }

    @Test
    void shouldReturnFalseIfUrlIsEmpty() {
        assertFalse(LinkValidator.isValid("", "aau.at"));
    }

    @Test
    void shouldReturnFalseIfProtocolIsInvalid() {
        assertFalse(LinkValidator.isValid("mailto:info@aau.at", "aau.at"));
    }

    @Test
    void shouldReturnFalseIfUrlHasSpaces(){
        assertFalse(LinkValidator.isValid("https://www .aau.at/", "aau.at"));
    }
}
