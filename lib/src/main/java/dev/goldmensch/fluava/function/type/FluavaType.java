package dev.goldmensch.fluava.function.type;

public record FluavaType<T>(
        Class<T> acceptableType,
        String defaultFunction
) {
}
