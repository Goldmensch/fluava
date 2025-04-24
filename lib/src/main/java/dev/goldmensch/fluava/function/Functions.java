package dev.goldmensch.fluava.function;

import dev.goldmensch.fluava.function.builtin.DatetimeFunction;
import dev.goldmensch.fluava.function.builtin.NumberFunction;
import dev.goldmensch.fluava.function.builtin.RawFunction;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class Functions {
    private static final String NUMBER = "NUMBER";
    private static final String RAW = "RAW";
    private static final String DATETIME = "DATETIME";

    private final Map<String, Function<?>> functions;

    public Functions(Map<String, Function<?>> functions) {
        this.functions = new HashMap<>(functions);

        this.functions.computeIfAbsent(NUMBER, __ -> new NumberFunction());
        this.functions.computeIfAbsent(RAW, __ -> new RawFunction());
        this.functions.computeIfAbsent(DATETIME, __ -> new DatetimeFunction());
    }

    public Optional<Value> tryImplicit(Locale locale, Object value) {
        Object realValue = value instanceof Partial(Object raw, var __)
                ? raw
                : value;

        String func = switch (realValue) {
            case Number __ -> NUMBER;
            case CharSequence __ -> RAW;
            case ZonedDateTime __ -> DATETIME;
            case LocalDateTime __ -> DATETIME;
            default -> null;
        };
        if (func == null) return Optional.empty();

        Value.Result result = call(locale, func, value, Map.of());
        return Optional.of(result);
    }

    public Value.Result call(Locale locale, String name, Object positional, Map<String, Object> named) {
        Function<?> function = functions.get(name);
        Map<String, Function.ParameterType> allowedParameters = function.allowedParameter();

        // check if localizer provided developer parameter
        named.forEach((key, value) -> {
            Function.ParameterType parameterType = allowedParameters.get(key);
            if (parameterType == Function.ParameterType.DEVELOPER) throw new IllegalArgumentException("Parameter %s is a developer parameter!".formatted(key));
        });

        Map.Entry<Object, Map<String, Object>> resolved = resolvePartial(name, positional, named);

        // check if all provided parameters are allowed
        named.forEach((key, value) -> {
            Function.ParameterType parameterType = allowedParameters.get(key);
            if (parameterType == null) throw new IllegalArgumentException("Parameter %s unsupported on function %s".formatted(key, name));
        });

        return function.apply(new Context(locale), resolved.getKey(), resolved.getValue());
    }

    private Map.Entry<Object, Map<String, Object>> resolvePartial(String id, Object value, Map<String, Object> named) {
        partial_check: if (value instanceof Partial(Object raw, Map<String, Map<String, Object>> defaultParameters)) {
            Map<String, Object> params = defaultParameters.get(id);
            if (params == null) break partial_check;

            HashMap<String, Object> copy = new HashMap<>(named);
            params.keySet().forEach(key -> copy.putIfAbsent(key, params.get(key)));

            return Map.entry(raw, copy);
        }

        return Map.entry(value, named);
    }
}
