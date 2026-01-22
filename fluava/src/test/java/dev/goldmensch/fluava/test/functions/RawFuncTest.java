package dev.goldmensch.fluava.test.functions;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class RawFuncTest {

    record Data(String foo) {}

    @Test
    void success() {
        Assertions.assertEquals("Data[foo=works]", Helpers.format("{ RAW($var) }", Map.of("var", new Data("works"))));
    }
}
