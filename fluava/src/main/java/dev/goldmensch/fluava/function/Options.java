package dev.goldmensch.fluava.function;

import io.github.kaktushose.proteus.Proteus;
import io.github.kaktushose.proteus.conversion.ConversionResult;
import io.github.kaktushose.proteus.type.Type;

import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/// The named arguments of a function call
public class Options {
    private final Map<String, Object> rawOptions;

    public Options(Map<String, Object> rawOptions) {
        this.rawOptions = rawOptions;
    }

    /// gets the argument associated with the given key
    ///
    /// @param key the arguments key
    /// @param type the variables class to what it should be converted (via proteus)
    /// @param fallback the fallback value, that should be returned if this argument isn't provided by the caller
    /// @return the provided value
    public <T> T get(String key, Class<T> type, T fallback) throws FunctionException {
        return entry(key).as(type, fallback);
    }

    /// gets the argument associated with the given key
    ///
    /// @param key the arguments key
    /// @param type the variables class to what it should be converted (via proteus)
    /// @throws FunctionException if there's no value associated with this key
    /// @return the provided value
    public <T> T get(String key, Class<T> type) throws FunctionException {
        return entry(key).as(type);
    }

    /// A utility method that comes in handy if some action should be done only if there's a value
    /// associated with the given key
    ///
    /// @param key the arguments key
    /// @param type the variables class to what it should be converted (via proteus)
    /// @param action the [Consumer] to be called with the associated value as the parameter
    public <T> void ifHasDo(String key, Class<T> type, Consumer<T> action) {
        if (!has(key)) return;
        action.accept(get(key, type));
    }

    /// @return whether the key is associated with a value
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

    /// An entry that pairs a key and the associated value
    public static class Entry {
        private final String key;
        private final Object raw;

        Entry(String key, Object raw) {
            this.key = key;
            this.raw = raw;
        }

        /// @return the passed key
        public String key() {
            return key;
        }

        /// gets the argument associated with the given key
        ///
        /// @param type the variables class to what it should be converted (via proteus)
        /// @param fallback the fallback value, that should be returned if this argument isn't provided by the caller
        /// @return the provided value
        public <T> T as(Class<T> type, T fallback) throws FunctionException {
            if (raw == null) return fallback;

            ConversionResult<T> result = Proteus.global().convert(raw, Type.dynamic(raw), Type.of(type));

            return switch (result) {
                case ConversionResult.Success(T val) -> val;
                case ConversionResult.Failure<?> failure -> throw new FunctionException("Couldn't convert option!: \n" + failure.detailedMessage());
            };
        }

        /// gets the argument associated with the given key
        ///
        /// @param type the variables class to what it should be converted (via proteus)
        /// @throws FunctionException if there's no value associated with this key
        /// @return the provided value
        public <T> T as(Class<T> type) throws FunctionException {
            if (raw == null) throw new FunctionException("Option '%s' may not be unset!".formatted(key));
            return as(type, null);
        }
    }
}
