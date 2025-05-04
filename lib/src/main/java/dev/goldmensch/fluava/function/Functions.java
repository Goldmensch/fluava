package dev.goldmensch.fluava.function;

import dev.goldmensch.fluava.function.builtin.DatetimeFunction;
import dev.goldmensch.fluava.function.builtin.NumberFunction;
import dev.goldmensch.fluava.function.builtin.RawFunction;
import dev.goldmensch.fluava.function.type.FluavaType;
import dev.goldmensch.fluava.function.type.FluavaValue;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.*;

public class Functions {
    private static final String NUMBER = "NUMBER";
    private static final String RAW = "RAW";
    private static final String DATETIME = "DATETIME";

    private final Map<Class<?>, FluavaType<?>> types = new HashMap<>();
    private final Map<String, Function<?>> functions;

    public Functions(Map<String, Function<?>> functions, Set<FluavaType<?>> types) {
        this.functions = new HashMap<>(functions);
        addTypes(types);


        // default types and functions
        addTypes(Set.of(
                new FluavaType<>(Number.class, NUMBER),
                new FluavaType<>(String.class, RAW),
                new FluavaType<>(ZonedDateTime.class, DATETIME),
                new FluavaType<>(LocalDateTime.class, DATETIME)
        ));

        this.functions.putIfAbsent(NUMBER, new NumberFunction());
        this.functions.putIfAbsent(RAW, new RawFunction());
        this.functions.putIfAbsent(DATETIME, new DatetimeFunction());
    }

    private void addTypes(Set<FluavaType<?>> types) {
        for (FluavaType<?> type : types) {
            this.types.putIfAbsent(type.acceptableType(), type);
        }
    }


    public Optional<Value> tryImplicit(Locale locale, Object value) {
        Object actualValue = value instanceof Partial(Object wrapped, var _)
                ? wrapped
                : value;

        FluavaValue<?> fluavaValue = toFluavaValue(actualValue);
        if (fluavaValue == null) return Optional.empty();

        Value.Result result = call(locale, fluavaValue.type().defaultFunction(), List.of(fluavaValue.value()), resolveParams(value, Map.of()));
        return Optional.of(result);
    }

    public Value.Result call(Locale locale, String name, List<Object> positional, Map<String, Object> named) {
        Function<?> function = functions.get(name);
        Context context = new Context(locale);

        outer: if (positional.size() == 1 && positional.getFirst() instanceof Partial(Object wrapped, var _)) {
            FluavaValue<?> value = toFluavaValue(wrapped);
            if (value == null || !value.type().defaultFunction().equals(name)) break outer;

            Map<String, Object> params = resolveParams(positional.getFirst(), named);
            return function.apply(context, List.of(wrapped), params);
        }

        return function.apply(context, positional, named);
    }

    // entry point for proteus
    @SuppressWarnings({"rawtypes", "unchecked"})
    private FluavaValue<?> toFluavaValue(Object value) {
        FluavaType type = types.values().stream()
                .filter(fluavaType -> fluavaType.acceptableType().isInstance(value))
                .findFirst()
                .orElse(null);
        if (type == null) return null;

        return new FluavaValue<>(value, type);
    }

    private Map<String, Object> resolveParams(Object value, Map<String, Object> named) {
        if (value instanceof Partial(var _, Map<String, Object> defaultParams)) {
            if (defaultParams == null || defaultParams.isEmpty()) return named;

            HashMap<String, Object> copy = new HashMap<>(named);
            copy.putAll(defaultParams);
            return copy;
        }
        return named;
    }
}
