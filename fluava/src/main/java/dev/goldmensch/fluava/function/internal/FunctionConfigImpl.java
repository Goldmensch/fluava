package dev.goldmensch.fluava.function.internal;

import dev.goldmensch.fluava.FluavaBuilder;
import dev.goldmensch.fluava.function.Function;

import java.util.HashMap;
import java.util.Map;

public class FunctionConfigImpl implements FluavaBuilder.FunctionConfig {

    private final Map<String, Function<?, ?>> functions = new HashMap<>();
    private boolean fallbackToString = false;

    @Override
    public FluavaBuilder.FunctionConfig register(String name, Function<?, ?> function) {
        functions.put(name, function);
        return this;
    }

    @Override
    public FluavaBuilder.FunctionConfig register(String name, Function<?, ?> function, boolean override) {
        if (override) {
            functions.put(name, function);
        } else {
            functions.putIfAbsent(name, function);
        }

        return this;
    }

    @Override
    public FluavaBuilder.FunctionConfig fallbackToString(boolean fallback) {
        fallbackToString = fallback;
        return this;
    }

    public Map<String, Function<?, ?>> functions() {
        return functions;
    }

    public boolean fallbackToString() {
        return fallbackToString;
    }
}
