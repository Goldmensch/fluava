package dev.goldmensch.message.internal;

import java.util.Map;

public record Task(
        StringBuilder builder,
        Map<String, Object> variables
) {
    public void append(Object value) {
        builder.append(value);
    }
}
