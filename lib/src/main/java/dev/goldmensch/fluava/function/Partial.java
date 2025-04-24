package dev.goldmensch.fluava.function;

import java.util.Map;

public record Partial(
        Object value,
        Map<String, Map<String, Object>> defaultParameters
) {}
