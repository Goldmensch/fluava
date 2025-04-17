package dev.goldmensch.function;

import dev.goldmensch.function.builtin.NumberFunction;
import dev.goldmensch.function.builtin.StringFunction;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class Functions {
    private final Map<String, Function<?>> functions;

    public Functions(Map<String, Function<?>> functions) {
        this.functions = new HashMap<>(functions);

        this.functions.computeIfAbsent(Defaults.NUMBER.name(), __ -> new NumberFunction());
        this.functions.computeIfAbsent(Defaults.STRING.name(), __ -> new StringFunction());
    }

    public Optional<Value.Result<?>> tryImplicit(Locale locale, Object value) {
        Function<?> func = switch (value) {
            case Number __ -> functions.get(Defaults.NUMBER.name());
            case CharSequence __ -> functions.get(Defaults.STRING.name());
            default -> null;
        };
        if (func == null) return Optional.empty();

        Value.Result<?> result = func.apply(new Context(locale), new Value.Variable(value), Map.of());
        return Optional.of(result);
    }

    public Value.Result<?> call(Locale locale, String name, Value<?> positional, Map<String, Value<?>> named) {
        return functions.get(name).apply(new Context(locale), positional, named);
    }

    enum Defaults {
        NUMBER,
        STRING
    }


}
