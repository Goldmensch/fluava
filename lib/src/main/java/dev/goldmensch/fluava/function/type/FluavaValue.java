package dev.goldmensch.fluava.function.type;

public record FluavaValue<T>(
        T value,
        FluavaType<T> type
) {
}
