package dev.goldmensch.fluava.function;

import dev.goldmensch.fluava.function.builtin.DatetimeFunction;
import dev.goldmensch.fluava.function.builtin.NumberFunction;
import dev.goldmensch.fluava.function.builtin.RawFunction;
import io.github.kaktushose.proteus.Proteus;
import io.github.kaktushose.proteus.conversion.ConversionResult;
import io.github.kaktushose.proteus.type.Type;

import java.util.*;

public class Functions {
    private static final String NUMBER = "NUMBER";
    private static final String RAW = "RAW";
    private static final String DATETIME = "DATETIME";

    private final Map<String, Function<?, ?>> functions;

    public Functions(Map<String, Function<?, ?>> functions) {
        this.functions = new HashMap<>(functions);

        this.functions.putIfAbsent(NUMBER, new NumberFunction());
        this.functions.putIfAbsent(RAW, new RawFunction());
        this.functions.putIfAbsent(DATETIME, new DatetimeFunction());
    }

    record Adapted<T>(T value, Function<?, T> defaultFunction) {}

    public Optional<Value> tryImplicit(Locale locale, Object value) {
        Object actualValue = value instanceof Partial(Object wrapped, var _)
                ? wrapped
                : value;

        return findImplicit(actualValue)
                .map(adapted -> call(locale, adapted.defaultFunction(), List.of(adapted.value()), resolveParams(value, Map.of())));
    }

    @SuppressWarnings("unchecked")
    public  <T> Value.Result call(Locale locale, String funcName, List<? extends T> positional, final Map<String, Object> named) {
        Function<?, T> function = (Function<?, T>) functions.get(funcName);

        return call(locale, function, positional, named);
    }

    private <T> Value.Result call(Locale locale, Function<?, T> function, List<? extends T> positional, final Map<String, Object> named) {
        Context context = new Context(locale);

        Map<String, Object> resolvedNamed = named;
        if (positional.size() == 1 && positional.getFirst() instanceof Partial(Object wrapped, var _)) {
            resolvedNamed = findImplicit(wrapped)
                    .filter(adapted -> adapted.defaultFunction.equals(function))
                    .map(_ -> resolveParams(positional.getFirst(), named))
                    .orElse(named);
        }

        return function.apply(context, positional, resolvedNamed);
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

    @SuppressWarnings("unchecked")
    private <T> Optional<Adapted<T>> findImplicit(Object value) {
        return functions.values()
                .stream()
                .filter(Function.Implicit.class::isInstance)
                .map(function -> (Function.Implicit<?, T>) function)
                .flatMap(function -> function.acceptableTypes()
                        .stream()
                        .map(type -> Proteus.global().convert(value, Type.dynamic(value), Type.of(type)))
                        .filter(result -> result instanceof ConversionResult.Success<? extends T>)
                        .map(result -> new Adapted<>(((ConversionResult.Success<? extends T>) result).value(), function))
                )
                .findAny();
    }
}
