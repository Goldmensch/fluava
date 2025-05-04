package dev.goldmensch.fluava.function;

import java.util.List;
import java.util.Map;

public interface Function<R extends Value.Result> {
    R apply(Context context, List<Object> positional, Map<String, Object> named);
}
