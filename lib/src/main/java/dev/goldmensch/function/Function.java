package dev.goldmensch.function;

import java.util.Map;

public interface Function<T extends Value.Result> {
    T apply(Context context, Object positional, Map<String, Object> named);
    Map<String, ParameterType> allowedParameter();

    enum ParameterType {
        DEVELOPER,
        UNIVERSAL
    }
}
