package dev.goldmensch.fluava.test.fluava;

import dev.goldmensch.fluava.Fluava;
import dev.goldmensch.fluava.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.Map;

class MessageTest {

    private static Fluava fluava;

    @BeforeAll
    static void init() {
        fluava = Fluava.create(Locale.ENGLISH);
    }

    @Test
    void unresolvedKey_withAttributeNotation_ShouldReturnOriginalKey() {
        final String key = "example.com";

        Resource resource = fluava.of("", Locale.ENGLISH).orElseThrow();

        Assertions.assertEquals(key, resource.message(key).apply(Map.of()));
    }
}
