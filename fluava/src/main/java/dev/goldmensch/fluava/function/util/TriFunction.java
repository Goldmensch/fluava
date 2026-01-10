package dev.goldmensch.fluava.function.util;

import java.util.function.Function;

/// A [Function] that takes three arguments and returns one result.
@FunctionalInterface
public interface TriFunction<F, S, T, R> {

    /// @see Function#apply(Object)
    R apply(F first, S second, T third);
}
