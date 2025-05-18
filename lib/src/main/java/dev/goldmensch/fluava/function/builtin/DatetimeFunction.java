package dev.goldmensch.fluava.function.builtin;

import dev.goldmensch.fluava.function.Context;
import dev.goldmensch.fluava.function.Function;
import dev.goldmensch.fluava.function.Options;
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
    public Value.Text apply(Context context, Temporal value, Options options) {
        ZonedDateTime time = switch (value) {
            case ZonedDateTime zT -> zT;
            case LocalDateTime local -> {
                String id = options.get("timeZone", String.class, TimeZone.getDefault().getID());
                yield ZonedDateTime.of(local, ZoneId.of(id));
            }

            default -> throw new UnsupportedOperationException();
        };

        Map<String, String> parameters = options.entries()
                .stream()
                .collect(Collectors.toMap(Options.Entry::key, a -> a.as(String.class)));

        String formatted = IntlDateTimeFormatter.format(context.locale(), time, parameters);

        return new Value.Text(formatted);
    }

    @Override
    public Collection<Class<? extends Temporal>> acceptableTypes() {
        return List.of(ZonedDateTime.class, LocalDate.class);
    }
}
