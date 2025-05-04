package dev.goldmensch.fluava.function.builtin;

import dev.goldmensch.fluava.function.Context;
import dev.goldmensch.fluava.function.Function;
import dev.goldmensch.fluava.function.Value;

import java.util.List;
import java.util.Map;

public class RawFunction implements Function<Value.Text> {

    @Override
    public Value.Text apply(Context context, List<Object> positional, Map<String, Object> named) {
        if (positional.getFirst() instanceof String string) {
            return new Value.Text(string);
        }

        throw new UnsupportedOperationException();
    }
}
