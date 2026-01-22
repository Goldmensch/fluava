package dev.goldmensch.fluava.test.functions;

import dev.goldmensch.fluava.Fluava;

import java.util.Locale;
import java.util.Map;

class Helpers {
    static String format(String expression, Locale locale, Map<String, Object> placeholder) {
        return Fluava.create(locale).of("msg = " + expression, locale).orElseThrow().message("msg").apply(placeholder);
    }

    static String format(String expression, Map<String, Object> placeholder) {
       return format(expression, Locale.ENGLISH, placeholder);
    }
}
