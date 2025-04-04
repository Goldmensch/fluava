package dev.goldmensch.cldrplurals;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;

public record PluralRule(
        Locale locale,
        EnumMap<PluralCategory, Predicate<Double>> cardinals,
        EnumMap<PluralCategory, Predicate<Double>> ordinals
) {
    public PluralCategory find(Type type, double number) {
        return switch (type) {
            case CARDINAL -> findCardinal(number);
            case ORDINAL -> findOrdinal(number);
        };
    }

    public PluralCategory findCardinal(double number) {
       return cardinals.entrySet()
                .stream()
                .filter(entry -> entry.getValue().test(number))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(PluralCategory.OTHER);
    }

    public PluralCategory findOrdinal(double number) {
        return ordinals.entrySet()
                .stream()
                .filter(entry -> entry.getValue().test(number))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(PluralCategory.OTHER);
    }
}
