package dev.goldmensch.fluava.test.functions;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static dev.goldmensch.fluava.test.functions.Helpers.format;

public class StringFuncTest {

    @Test
    void explicit_call() {
        Assertions.assertEquals("ja geht", format("{ STRING(\"ja geht\") }", Map.of()));
    }
}
