package dev.goldmensch.function.internal;

import java.time.ZonedDateTime;
import java.time.chrono.Chronology;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiFunction;

public class IntlDateTimeFormatter {


    public static String format(Locale locale, ZonedDateTime time, Map<String, String> overrides) {
        String baseFormat = DateTimeFormatterBuilder.getLocalizedDateTimePattern(FormatStyle.FULL, FormatStyle.FULL, Chronology.ofLocale(locale), locale);
        List<String> parts = parts(baseFormat);

        // hour12 modify
        parts = modify(parts, (part, type) -> {
            if (type == Type.HOUR) {
                String hour12 = overrides.get("hour12");
                if (hour12 == null) return null;

                String modified;
                if ("true".equals(hour12)) {
                    modified = part.replace("H", "K").replace("k", "K");
                } else {
                    modified = part.replace("K", "H").replace("h", "k");
                }
                return List.of(modified);
            }

            return null;
        });

        parts = modify(parts, (part, type) -> {
            String value = overrides.get(type.name);
            if (value == null) return null;

            int i = type.repetitions(value);
            String newFormat = part.substring(0, 1).repeat(i);
            return List.of(newFormat);
        });

        String format = String.join("", parts);
        return DateTimeFormatter.ofPattern(format, locale).format(time);
    }

    private static List<String> modify(List<String> parts, BiFunction<String, Type, List<String>> action) {
        ArrayList<String> modified = new ArrayList<>();

        for (String c : parts) {
            String trim = c.trim();
            if (trim.isEmpty()) {
                modified.add(c);
                continue;
            }

            Type type = Type.forSymbol(trim.charAt(0));
            if (type == null) {
                modified.add(c);
                continue;
            }

            List<String> output = action.apply(c, type);
            if (output == null) {
                modified.add(c);
            } else {
                modified.addAll(output);
            }
        }

        return modified;
    }

    private static List<String> parts(String format) {
        List<String> parts = new ArrayList<>();

        StringBuilder p = new StringBuilder();
        boolean inline = false;
        for (int i = 0; i < format.length(); i++) {
            char c = format.charAt(i);
            if (c == '\'') {
                inline = !inline;
                p.append(c);
                continue;
            }

            if (inline) {
                p.append(c);
            } else {
                if (i != 0 && format.charAt(i - 1) != c) {
                    parts.add(p.toString());
                    p.setLength(0);
                }

                p.append(c);
            }
        }
        parts.add(p.toString());

        return parts;
    }

    private enum StyleType {
        TEXT(Map.of(
                "short", 1,
                "long", 4,
                "narrow", 5
        )),
        NUM(Map.of(
                "numeric", 1,
                "2-digit", 2
        ));


        private final Map<String, Integer> format;

        StyleType(Map<String, Integer> format) {
            this.format = format;
        }
    }

    private enum Type {
        WEEKDAY("weekday", StyleType.TEXT),
        ERA("era", StyleType.TEXT),
        YEAR("year", StyleType.NUM),
        MONTH("month", null),
        DAY("day", StyleType.NUM),
        HOUR("hour", StyleType.NUM),
        MINUTE("minute", StyleType.NUM),
        SECOND("second", StyleType.NUM),
        TIME_ZONE_NAME("timeZoneName", StyleType.TEXT);

        private final String name;
        private final StyleType styleType;

        Type(String name, StyleType styleType) {
            this.name = name;
            this.styleType = styleType;
        }

        private int repetitions(String value) {
            if (this == MONTH) {
                Integer first = StyleType.TEXT.format.get(value);
                if (first != null) return first;
                return StyleType.NUM.format.get(value);
            }
            return styleType.format.get(value);
        }

        static Type forSymbol(char symbol) {
            return switch (symbol) {
                case 'E' -> WEEKDAY;
                case 'G' -> ERA;
                case 'u' -> YEAR;
                case 'M', 'L' -> MONTH;
                case 'd' -> DAY;
                case 'H', 'h', 'K', 'k' -> HOUR;
                case 'm' -> MINUTE;
                case 's' -> SECOND;
                case 'z' -> TIME_ZONE_NAME;
                default -> null;
            };
        }
    }
}
