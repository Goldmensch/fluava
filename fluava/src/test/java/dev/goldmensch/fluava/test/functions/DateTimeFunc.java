package dev.goldmensch.fluava.test.functions;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Locale;
import java.util.Map;

import static dev.goldmensch.fluava.test.functions.Helpers.format;

public class DateTimeFunc {

    LocalDateTime badDay = LocalDateTime.of(2025, 1, 20, 19, 45);

    @Test
    void implicit_LocalDateTime_german() {
        String result = format("{ $var }", Locale.GERMAN, Map.of("var", badDay));
        Assertions.assertEquals("Montag, 20. Januar 2025, 19:45:00 Mitteleuropäische Normalzeit", result);
    }

    @Test
    void implicit_LocalDateTime_english() {
        String result = format("{ $var }", Locale.ENGLISH, Map.of("var", badDay));
        Assertions.assertEquals("Monday, January 20, 2025, 7:45:00\u202FPM Central European Standard Time", result); // \u202F -> narrow no-break space
    }

    @Test
    void implicit_ZonedDateTime_german() {
        ZonedDateTime zonedBadDay = ZonedDateTime.of(badDay, ZoneId.of("America/New_York"));

        String result = format("{ $var }", Locale.GERMAN, Map.of("var", zonedBadDay));
        Assertions.assertEquals("Montag, 20. Januar 2025, 19:45:00 Nordamerikanische Ostküsten-Normalzeit", result);
    }

    @Test
    void implicit_ZonedDateTime_english() {
        ZonedDateTime zonedBadDay = ZonedDateTime.of(badDay, ZoneId.of("America/New_York"));

        String result = format("{ $var }", Locale.ENGLISH, Map.of("var", zonedBadDay));
        Assertions.assertEquals("Monday, January 20, 2025, 7:45:00\u202FPM Eastern Standard Time", result); // \u202F -> narrow no-break space
    }

    @Test
    void explicit_standard() {
        String result = format("{ DATETIME($var) }", Locale.ENGLISH, Map.of("var", badDay));
        Assertions.assertEquals("Monday, January 20, 2025, 7:45:00\u202FPM Central European Standard Time", result);
    }


}
