package dev.goldmensch.cldrplurals;

import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;

public record PluralRule(
        Locale locale,
        Map<PluralCategory, Predicate<String>> cardinals,
        Map<PluralCategory, Predicate<String>> ordinals
) {
    public PluralRule {
        cardinals = Map.copyOf(cardinals);
        ordinals = Map.copyOf(ordinals);
    }

    public PluralCategory find(Type type, String number) {
        return switch (type) {
            case CARDINAL -> findCardinal(number);
            case ORDINAL -> findOrdinal(number);
        };
    }

    public PluralCategory findCardinal(String number) {
       return find(cardinals, number);
    }

    public PluralCategory findOrdinal(String number) {
        return find(ordinals, number);
    }

    private PluralCategory find(Map<PluralCategory, Predicate<String>> map, String number) {
        return map.entrySet()
                .stream()
                .filter(entry -> entry.getValue().test(number))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(PluralCategory.OTHER);
    }
}
