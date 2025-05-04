package dev.goldmensch.fluava.function.builtin;

import dev.goldmensch.fluava.function.Context;
import dev.goldmensch.fluava.function.Function;
import dev.goldmensch.fluava.function.Value;
import dev.goldmensch.fluava.function.internal.IntlDateTimeFormatter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Collectors;

public class DatetimeFunction implements Function<Value.Text> {
    @Override
    public Value.Text apply(Context context, List<Object> positional, Map<String, Object> named) {
        ZonedDateTime time = switch (positional.getFirst()) {
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
}
