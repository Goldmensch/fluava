package dev.goldmensch.fluava.function;

import java.util.Collection;

public interface Function<R extends Value.Result, T> {
    R apply(Context context, Arguments<T> arguments, Options options);

    interface Implicit<R extends Value.Result, T> extends Function<R, T> {
        R apply(Context context, T value, Options options);

        Collection<Class<? extends T>> acceptableTypes();

        @Override
        default R apply(Context context, Arguments<T> arguments, Options options) {

            // try to convert to type that is matching one of the supported ones, kinda band-aid but works :D
            T convertedType = acceptableTypes()
                    .stream()
                    .flatMap(klass -> arguments.tryGet(0, klass).stream())
                    .findAny()
                    .orElseThrow();

            return apply(context, convertedType, options);
        }

    }
}
