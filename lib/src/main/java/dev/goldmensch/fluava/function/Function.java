package dev.goldmensch.fluava.function;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface Function<R extends Value.Result, T> {
    R apply(Context context, List<? extends T> positional, Map<String, Object> named);

    interface Implicit<R extends Value.Result, T> extends Function<R, T> {
        R apply(Context context, T value, Map<String, Object> named);

        Collection<Class<? extends T>> acceptableTypes();

        @Override
        default R apply(Context context, List<? extends T> positional, Map<String, Object> named) {
            return apply(context, positional.getFirst(), named);
        }

    }
}
