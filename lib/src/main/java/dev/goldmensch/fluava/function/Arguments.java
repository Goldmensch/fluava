package dev.goldmensch.fluava.function;

import dev.goldmensch.fluava.Result;
import io.github.kaktushose.proteus.Proteus;
import io.github.kaktushose.proteus.conversion.ConversionResult;
import io.github.kaktushose.proteus.type.Type;

import java.util.List;

public class Arguments<R> {
    private final List<? extends R> positional;

    public Arguments(List<? extends R> positional) {
        this.positional = positional;
    }

    protected <T> Result<T> tryGetFirst(Class<T> klass) {
        R raw = positional.getFirst();

        if (raw == null) return new Result.Failure<>("Given variable was null or not passed.");

        ConversionResult<T> result = Proteus.global().convert(raw, Type.dynamic(raw), Type.of(klass));

        return switch (result) {
            case ConversionResult.Success(T val) -> new Result.Success<>(val);
            case ConversionResult.Failure<?> failure -> new Result.Failure<>(failure.detailedMessage());
        };
    }


    public <T> T get(int number, Class<T> klass) throws FunctionException {
        R raw = positional.get(number);

        if (raw == null) throw new FunctionException("Given variable was null or not passed.");

        ConversionResult<T> result = Proteus.global().convert(raw, Type.dynamic(raw), Type.of(klass));

        return switch (result) {
            case ConversionResult.Success(T val) -> val;
            case ConversionResult.Failure<?> failure -> throw new FunctionException("Couldn't convert positional argument!: \n" + failure.detailedMessage());
        };
    }
}
