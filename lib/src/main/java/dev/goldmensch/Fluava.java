package dev.goldmensch;

import dev.goldmensch.function.Function;
import dev.goldmensch.function.Functions;

import java.util.Map;

public class Fluava {
    private final Functions functions;

    public Fluava(Map<String, Function<?>> functions) {
        this.functions = new Functions(functions);
    }
}
