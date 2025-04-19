package dev.goldmensch.function.builtin;

import dev.goldmensch.function.Context;
import dev.goldmensch.function.Function;
import dev.goldmensch.function.Value;

import java.util.Map;

public class StringFunction implements Function<Value.Text> {

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
