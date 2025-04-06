package dev.goldmensch.cldrplurals;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class Plurals {
    private static final Map<Locale, PluralRule> rules;

    private Plurals() {}

    static {
        rules = ServiceLoader.load(RulesProvider.class)
                .stream()
                .map(ServiceLoader.Provider::get)
                .map(RulesProvider::rules)
                .flatMap(Collection::stream)
                .collect(Collectors.toUnmodifiableMap(PluralRule::locale, Function.identity()));
    }

    public static Map<Locale, PluralRule> registered() {
        return rules;
    }

    public static PluralCategory find(Locale locale, Type type, String number) {
        PluralRule rules = forLocale(locale);
        if (rules == null) return null;

        return rules.find(type, number);
    }

    public static PluralRule forLocale(Locale locale) {
        return rules.get(locale);
    }
}
