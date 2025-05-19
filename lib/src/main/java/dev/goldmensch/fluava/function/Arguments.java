package dev.goldmensch.fluava.function;

import io.github.kaktushose.proteus.Proteus;
import io.github.kaktushose.proteus.conversion.ConversionResult;
import io.github.kaktushose.proteus.type.Type;

import java.util.List;
import java.util.Optional;

public class Arguments<R> {
    private final List<? extends R> positional;

    public Arguments(List<? extends R> positional) {
        this.positional = positional;
    }

    protected <T> Optional<T> tryGet(int index, Class<T> klass) {
        R raw = positional.get(index);
        ConversionResult<T> result = Proteus.global().convert(raw, Type.dynamic(raw), Type.of(klass));

        return switch (result) {
            case ConversionResult.Success(T val) -> Optional.of(val);
            case ConversionResult.Failure<?> _ -> Optional.empty();
        };
    }


    public <T> T get(int number, Class<T> klass) {
        R raw = positional.get(number);
        ConversionResult<T> result = Proteus.global().convert(raw, Type.dynamic(raw), Type.of(klass));

        return switch (result) {
            case ConversionResult.Success(T val) -> val;
            case ConversionResult.Failure<?> failure -> throw new IllegalStateException("Couldn't convert positional argument!: \n" + failure.detailedMessage());
        };
    }
}
