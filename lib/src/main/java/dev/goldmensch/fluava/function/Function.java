package dev.goldmensch.fluava.function;

import dev.goldmensch.fluava.Result;

import java.util.Collection;

public interface Function<R extends Value.Formatted, T> {
    Result<R> apply(Context context, Arguments<T> arguments, Options options) throws FunctionException;

    interface Implicit<R extends Value.Formatted, T> extends Function<R, T> {
        Result<R> apply(Context context, T value, Options options) throws FunctionException;

        Collection<Class<? extends T>> acceptableTypes();

        @SuppressWarnings("unchecked")
        @Override
        default Result<R> apply(Context context, Arguments<T> arguments, Options options) throws FunctionException {

            // try to convert to type that is matching one of the supported ones, kinda band-aid but works :D
            Result<T> convertedType = acceptableTypes()
                    .stream()
                    .map(arguments::tryGetFirst)
                    .filter(result -> result instanceof Result.Success<? extends T>)
                    .findAny()
                    .map(success -> (Result<T>) success)
                    .orElseGet(() -> new Result.Failure<>("Couldn't convert given type to any of the acceptable ones."));

            if (convertedType instanceof Result.Failure<T> failure) return failure.to();

            // safe to call orElseThrow since checked above
            return apply(context, convertedType.orElseThrow(), options);
        }

    }
}
