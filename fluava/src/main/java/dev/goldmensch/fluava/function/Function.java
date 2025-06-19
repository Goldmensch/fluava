package dev.goldmensch.fluava.function;

import dev.goldmensch.fluava.Result;

import java.util.Collection;


/// This interface models the functions described by project fluent.
///
/// A function in general accepts a number of positional arguments (here [Arguments] )
/// and a number of named arguments (here [Options] ) and returns a [Result] containing
/// either [Value.Number] and [Value.Text].
///
/// ## Implicit Functions
/// [Function.Implicit] is a special case, were the function only takes one positional argument, the "value".
/// This special sort of function is automatically applied if any of the variables match (or can be converted via proteus to) a class
/// specified in [Implicit#acceptableTypes()].
public interface Function<R extends Value.Formatted, T> {

    /// Applies this function to the provided value and options.
    ///
    /// @param context information like the locale
    /// @param arguments the positional arguments of this function
    /// @param options the named arguments/options to "configure" this function
    Result<R> apply(Context context, Arguments<T> arguments, Options options) throws FunctionException;

    /// An implicit function is a special case of a function, that can be automatically applied to a certain variable
    /// based on the variables type (class). It will be applied if the variables class matches one specified by
    /// [#acceptableTypes()] or can the converted by proteus to such.
    ///
    /// @param <R> The returned type of Value, either [Value.Number] or [Value.Text]
    /// @param <T> the base type of the supported types declared by [#acceptableTypes()]
    interface Implicit<R extends Value.Formatted, T> extends Function<R, T> {

        /// Applies this function to the provided value and options.
        ///
        /// @param context information like the locale
        /// @param value the object that should be formatted by this function
        /// @param options the named arguments/options to "configure" this function
        Result<R> apply(Context context, T value, Options options) throws FunctionException;

        /// @return the supported classes of this function
        Collection<Class<? extends T>> acceptableTypes();

        /// Just a default implementation, that calls [#apply(Context, Object, Options)]
        /// with the first argument of [Arguments]
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
