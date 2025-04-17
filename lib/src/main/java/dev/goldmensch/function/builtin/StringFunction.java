package dev.goldmensch.function.builtin;

import dev.goldmensch.function.Context;
import dev.goldmensch.function.Function;
import dev.goldmensch.function.Value;

import java.util.Map;

public class StringFunction implements Function<String> {
    @Override
    public Value.Result<String> apply(Context context, Value<?> argument, Map<String, Value<?>> arguments) {

        if (argument.value() instanceof String string) {
            return new Value.Text(string);
        }

        throw new UnsupportedOperationException();
    }
}
