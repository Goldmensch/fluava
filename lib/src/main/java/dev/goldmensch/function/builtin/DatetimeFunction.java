package dev.goldmensch.function.builtin;

import dev.goldmensch.function.Context;
import dev.goldmensch.function.Function;
import dev.goldmensch.function.Value;
import dev.goldmensch.function.internal.IntlDateTimeFormatter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Collectors;

public class DatetimeFunction implements Function<Value.Text> {
    @Override
    public Value.Text apply(Context context, Object positional, Map<String, Object> named) {
        ZonedDateTime time = switch (positional) {
            case ZonedDateTime zT -> zT;
            case LocalDateTime local -> {
                String id = (String) named.getOrDefault("timeZone", TimeZone.getDefault().getID());
                yield ZonedDateTime.of(local, ZoneId.of(id));
            }
            default -> throw new UnsupportedOperationException();
        };

        Map<String, String> parameters = named.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, a -> (String) a.getValue()));

        String formatted = IntlDateTimeFormatter.format(context.locale(), time, parameters);

        return new Value.Text(formatted);
    }


    @Override
    public Map<String, ParameterType> allowedParameter() {
        return Map.ofEntries(
                Map.entry("hour12", ParameterType.UNIVERSAL),
                Map.entry("weekday", ParameterType.UNIVERSAL),
                Map.entry("era", ParameterType.UNIVERSAL),
                Map.entry("year", ParameterType.UNIVERSAL),
                Map.entry("month", ParameterType.UNIVERSAL),
                Map.entry("day", ParameterType.UNIVERSAL),
                Map.entry("hour", ParameterType.UNIVERSAL),
                Map.entry("minute", ParameterType.UNIVERSAL),
                Map.entry("second", ParameterType.UNIVERSAL),
                Map.entry("timeZoneName", ParameterType.UNIVERSAL),
                Map.entry("timeZone", ParameterType.DEVELOPER)
        );
    }
}
