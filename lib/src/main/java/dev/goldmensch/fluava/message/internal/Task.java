package dev.goldmensch.fluava.message.internal;

import java.util.Locale;
import java.util.Map;

public record Task(
        Locale locale,
        StringBuilder builder,
        Map<String, Object> variables
) {
    public void append(Object value) {
        builder.append(value);
    }
}
