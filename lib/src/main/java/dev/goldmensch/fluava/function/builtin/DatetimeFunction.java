package dev.goldmensch.fluava.function.builtin;

import dev.goldmensch.fluava.function.Context;
import dev.goldmensch.fluava.function.Function;
import dev.goldmensch.fluava.function.Value;
import dev.goldmensch.fluava.function.internal.IntlDateTimeFormatter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Collectors;

public class DatetimeFunction implements Function.Implicit<Value.Text, Temporal> {

    @Override
    public Value.Text apply(Context context, Temporal value, Map<String, Object> named) {
        ZonedDateTime time = switch (value) {
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
    public Collection<Class<? extends Temporal>> acceptableTypes() {
        return List.of(ZonedDateTime.class, LocalDate.class);
    }
}
