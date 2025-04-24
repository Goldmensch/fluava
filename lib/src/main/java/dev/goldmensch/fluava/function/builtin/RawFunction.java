package dev.goldmensch.fluava.function.builtin;

import dev.goldmensch.fluava.function.Context;
import dev.goldmensch.fluava.function.Function;
import dev.goldmensch.fluava.function.Value;

import java.util.Map;

public class RawFunction implements Function<Value.Text> {

    @Override
    public Value.Text apply(Context context, Object positional, Map<String, Object> named) {
        if (positional instanceof String string) {
            return new Value.Text(string);
        }

        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, ParameterType> allowedParameter() {
        return Map.of();
    }
}
