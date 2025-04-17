package dev.goldmensch.function.builtin;

import dev.goldmensch.function.Context;
import dev.goldmensch.function.Function;
import dev.goldmensch.function.Value;

import java.text.NumberFormat;
import java.util.Map;

public class NumberFunction implements Function<Double> {

    @Override
    public Value.Result<Double> apply(Context context, Value<?> argument, Map<String, Value<?>> arguments) {

        if (argument.value() instanceof Number number) {
            double value = number.doubleValue();
            return new Value.Number(NumberFormat.getNumberInstance(context.locale()).format(value), value);
        }

        throw new UnsupportedOperationException();
    }
}
