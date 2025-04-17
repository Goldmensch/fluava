package dev.goldmensch.function;

import java.util.Map;

public interface Function<T> {
    Value.Result<T> apply(Context context, Value<?> argument, Map<String, Value<?>> arguments);
}
