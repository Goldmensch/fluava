package dev.goldmensch.fluava;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public sealed interface Result<T> {
    record Success<T>(T value) implements Result<T> {}
    record Failure<T>(String error) implements Result<T> {

        @SuppressWarnings("unchecked")
        public <R> Failure<R> to() {
            return (Failure<R>) this;
        }
    }

    default T orElseThrow() {
        return toOptional().orElseThrow();
    }

    default T orElse(T other) {
        return toOptional().orElse(other);
    }

    default T orElseGet(Supplier<? extends T> other) {
        return toOptional().orElseGet(other);
    }

    default <X extends Throwable> T orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
        return toOptional().orElseThrow(exceptionSupplier);
    }

    default Optional<T> toOptional() {
        return switch (this) {
            case Success(T value) -> Optional.of(value);
            case Failure<T> _ -> Optional.empty();
        };
    }

    default Optional<T> toOptional(Consumer<String> loggingAction) {
        if (this instanceof Failure<T>(String error)) {
            loggingAction.accept(error);
        }
        return toOptional();
    }
}
