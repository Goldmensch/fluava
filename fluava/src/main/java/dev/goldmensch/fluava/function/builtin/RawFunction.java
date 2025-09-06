package dev.goldmensch.fluava.function.builtin;

import dev.goldmensch.fluava.Result;
import dev.goldmensch.fluava.function.*;

public class RawFunction implements Function<Value.Text, Object> {

    @Override
    public Result<Value.Text> apply(Context context, Arguments<Object> arguments, Options options) throws FunctionException {
        return new Result.Success<>(new Value.Text(arguments.get(0, Object.class).toString()));
    }
}
