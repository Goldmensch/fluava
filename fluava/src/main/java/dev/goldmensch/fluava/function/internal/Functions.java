package dev.goldmensch.fluava.function.internal;

import dev.goldmensch.fluava.Result;
import dev.goldmensch.fluava.function.*;
import dev.goldmensch.fluava.function.builtin.DatetimeFunction;
import dev.goldmensch.fluava.function.builtin.NumberFunction;
import dev.goldmensch.fluava.function.builtin.RawFunction;
import io.github.kaktushose.proteus.Proteus;
import io.github.kaktushose.proteus.conversion.ConversionResult;
import io.github.kaktushose.proteus.type.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class Functions {
    private static final String NUMBER = "NUMBER";
    private static final String RAW = "RAW";
    private static final String DATETIME = "DATETIME";
    private static final Logger log = LoggerFactory.getLogger(Functions.class);

    private final Map<String, Function<?, ?>> functions;

    public Functions(Map<String, Function<?, ?>> functions) {
        this.functions = new HashMap<>(functions);

        this.functions.putIfAbsent(NUMBER, new NumberFunction());
        this.functions.putIfAbsent(RAW, new RawFunction());
        this.functions.putIfAbsent(DATETIME, new DatetimeFunction());
    }

    record Adapted<T>(T value, Function.Implicit<?, T> defaultFunction) {}

    public Optional<Value> tryImplicit(Locale locale, Object value) {
        Object actualValue = value instanceof Partial(Object wrapped, var _)
                ? wrapped
                : value;

        return findImplicit(actualValue)
                .flatMap(adapted ->
                        callFunction(adapted.defaultFunction, new Context(locale), new Arguments<>(List.of(adapted.value)), new Options(resolveParams(value, Map.of())))
                        .toOptional(log::warn)
                );
    }

    @SuppressWarnings("unchecked")
    public  <T> Optional<? extends Value.Formatted> call(Locale locale, String funcName, List<? extends T> positional, final Map<String, Object> named) {
        Function<?, T> function = (Function<?, T>) functions.get(funcName);

        Context context = new Context(locale);

        Map<String, Object> resolvedNamed = named;
        if (positional.size() == 1 && positional.getFirst() instanceof Partial(Object wrapped, var _)) {
            resolvedNamed = findImplicit(wrapped)
                    .filter(adapted -> adapted.defaultFunction.equals(function))
                    .map(_ -> resolveParams(positional.getFirst(), named))
                    .orElse(named);
        }

        return callFunction(function, context, new Arguments<>(positional), new Options(resolvedNamed))
                .toOptional(log::warn);
    }

    private <R extends Value.Formatted, T> Result<R> callFunction(Function<R, T> function, Context context, Arguments<T> arguments, Options options) {
        try {
            return function.apply(context, arguments, options);
        } catch (FunctionException e) {
            return new Result.Failure<>(e.getMessage());
        }
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
        if (value == null) return Optional.empty();
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
