package dev.goldmensch.fluava.function.builtin;

import dev.goldmensch.fluava.Result;
import dev.goldmensch.fluava.function.Context;
import dev.goldmensch.fluava.function.Function;
import dev.goldmensch.fluava.function.Options;
import dev.goldmensch.fluava.function.Value;

import java.util.Collection;
import java.util.List;

public class StringFunction implements Function.Implicit<Value.Text, String> {

    @Override
    public Result<Value.Text> apply(Context context, String value, Options options) {
        return new Result.Success<>(new Value.Text(value));
    }

    @Override
    public Collection<Class<? extends String>> acceptableTypes() {
        return List.of(String.class);
    }
}
