package dev.goldmensch.fluava.function;

import io.github.kaktushose.proteus.Proteus;
import io.github.kaktushose.proteus.conversion.ConversionResult;
import io.github.kaktushose.proteus.type.Type;

import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Options {
    private final Map<String, Object> rawOptions;

    public Options(Map<String, Object> rawOptions) {
        this.rawOptions = rawOptions;
    }

    public <T> T get(String key, Class<T> type, T fallback) throws FunctionException {
        return entry(key).as(type, fallback);
    }

    public <T> T get(String key, Class<T> type) throws FunctionException {
        return entry(key).as(type);
    }

    public <T> void ifHasDo(String key, Class<T> type, Consumer<T> action) throws FunctionException {
        if (!has(key)) return;
        action.accept(get(key, type));
    }

    public boolean has(String key) {
        return rawOptions.containsKey(key);
    }

    public Entry entry(String key) {
        return new Entry(key, rawOptions.get(key));
    }

    public Set<Entry> entries() {
        return rawOptions.entrySet()
                .stream()
                .map(entry -> new Entry(entry.getKey(), entry.getValue()))
                .collect(Collectors.toUnmodifiableSet());
    }

    public static class Entry {
        private final String key;
        private final Object raw;

        public Entry(String key, Object raw) {
            this.key = key;
            this.raw = raw;
        }

        public String key() {
            return key;
        }

        public <T> T as(Class<T> type, T fallback) throws FunctionException {
            if (raw == null) return fallback;

            ConversionResult<T> result = Proteus.global().convert(raw, Type.dynamic(raw), Type.of(type));

            return switch (result) {
                case ConversionResult.Success(T val) -> val;
                case ConversionResult.Failure<?> failure -> throw new FunctionException("Couldn't convert option!: \n" + failure.detailedMessage());
            };
        }

        public <T> T as(Class<T> type) throws FunctionException {
            if (raw == null) throw new FunctionException("Option '%s' may not be unset!".formatted(key));
            return as(type, null);
        }
    }
}
