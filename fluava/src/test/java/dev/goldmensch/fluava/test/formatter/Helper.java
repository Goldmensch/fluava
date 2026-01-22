package dev.goldmensch.fluava.test.formatter;

import dev.goldmensch.fluava.Fluava;
import dev.goldmensch.fluava.Resource;
import dev.goldmensch.fluava.ast.tree.pattern.Pattern;
import dev.goldmensch.fluava.function.internal.FunctionConfigImpl;
import dev.goldmensch.fluava.function.internal.Functions;
import dev.goldmensch.fluava.internal.Formatter;

import java.util.Locale;
import java.util.Map;

class Helper {
    static String format(Pattern pattern) {
        return format(pattern, Map.of());
    }

    static String format(Pattern pattern, Map<String, Object> placeholders) {
        Functions functions = new Functions(new FunctionConfigImpl());
        Resource resource = Fluava.create(Locale.ENGLISH).of("", Locale.ENGLISH).orElseThrow();
        return new Formatter(functions, resource, pattern, "msg").apply(Locale.ENGLISH, placeholders);
    }
}
