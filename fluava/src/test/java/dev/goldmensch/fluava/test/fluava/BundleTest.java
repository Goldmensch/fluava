package dev.goldmensch.fluava.test.fluava;

import dev.goldmensch.fluava.Bundle;
import dev.goldmensch.fluava.Fluava;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.Map;

public class BundleTest {

    private Bundle msgBundle() {
        return Fluava.create(Locale.GERMAN).loadBundle("bundle/msg");
    }

    private Bundle dirBundle() {
        return Fluava.create(Locale.GERMAN).loadBundle("bundle/dir");
    }

    @Test
    void german() {
        Assertions.assertEquals("Deutsch", msgBundle().apply(Locale.GERMAN, "language", Map.of()));
    }

    @Test
    void swiss_german() {
        Assertions.assertEquals("Schweizerdeutsch", msgBundle().apply(Locale.of("de", "ch"), "language", Map.of()));
    }

    @Test
    void swiss_german_1901() {
        Assertions.assertEquals("Schweizerdeutsch 1901 Reform", msgBundle().apply(Locale.of("de", "ch", "1901"), "language", Map.of()));
    }

    @Test
    void base_directory_german() {
        Assertions.assertEquals("Deutsch", dirBundle().apply(Locale.GERMAN, "language", Map.of()));
    }

    @Test
    void base_directory_swiss_german() {
        Assertions.assertEquals("Schweizerdeutsch", dirBundle().apply(Locale.of("de", "ch"), "language", Map.of()));
    }

    @Test
    void base_directory_swiss_german_1901() {
        Assertions.assertEquals("Schweizerdeutsch 1901 Reform", dirBundle().apply(Locale.of("de", "ch", "1901"), "language", Map.of()));
    }

    @Test
    void fallback_wrong_language() {
        Assertions.assertEquals("Deutsch", dirBundle().apply(Locale.ENGLISH, "language", Map.of()));
    }

    @Test
    void fallback_wrong_country() {
        Assertions.assertEquals("Deutsch", dirBundle().apply(Locale.GERMANY, "language", Map.of()));
    }

    @Test
    void fallback_wrong_variant() {
        Assertions.assertEquals("Schweizerdeutsch", dirBundle().apply(Locale.of("de", "ch", "invalid"), "language", Map.of()));
    }

    @Test
    void fallback_to_message_of_lower_specialization() {
        Assertions.assertEquals("Nur in der Schweiz",
                dirBundle().apply(Locale.of("de", "ch", "1901"), "only-in-country", Map.of()));
    }

    @Test
    void root_bundle_in_fluava() {
        Bundle bundle = Fluava.builder()
                .fallback(Locale.GERMAN)
                .bundleRoot("bundle")
                .build()
                .loadBundle("msg");

        Assertions.assertEquals("Deutsch", bundle.apply(Locale.GERMAN, "language", Map.of()));
    }


}
