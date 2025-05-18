package dev.goldmensch.fluava.function;

import java.util.Collection;
import java.util.List;

public interface Function<R extends Value.Result, T> {
    R apply(Context context, List<? extends T> positional, Options options);

    interface Implicit<R extends Value.Result, T> extends Function<R, T> {
        R apply(Context context, T value, Options options);

        Collection<Class<? extends T>> acceptableTypes();

        @Override
        default R apply(Context context, List<? extends T> positional, Options options) {
            return apply(context, positional.getFirst(), options);
        }

    }
}
