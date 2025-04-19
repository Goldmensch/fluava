package dev.goldmensch.resource;

import dev.goldmensch.ast.FluentParser;
import dev.goldmensch.ast.tree.entry.Term;
import dev.goldmensch.function.Functions;
import dev.goldmensch.function.Partial;
import dev.goldmensch.message.Message;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class Resource {
    private final Map<String, Message> messages;
    private final Map<String, Message> terms;

    public Resource(Functions functions, Locale locale, dev.goldmensch.ast.tree.Resource resource) {
        this.terms = resource.components()
                .stream()
                .filter(Term.class::isInstance)
                .map(Term.class::cast)
                .collect(Collectors.toUnmodifiableMap(Term::id, term -> new Message(functions, locale, this, term)));

        this.messages = resource.components()
                .stream()
                .filter(dev.goldmensch.ast.tree.message.Message.class::isInstance)
                .map(dev.goldmensch.ast.tree.message.Message.class::cast)
                .collect(Collectors.toUnmodifiableMap(dev.goldmensch.ast.tree.message.Message::id, msg -> new Message(functions, locale, this, msg)));
    }

    public Message message(String key) {
        return messages.get(key);
    }

    public Message term(String key) {
        return terms.get(key);
    }

    public static void main(String[] args) {
        String text = """
                info = The price is { NUMBER($price, currencyDisplay: "name") }
                test = The number is { $num }
                date = Today is { DATETIME($date, hour12: "false", weekday: "short", timeZoneName: "short") }
                """;

        dev.goldmensch.ast.tree.Resource resourceAst = new FluentParser().apply(text);
        Resource resource = new Resource(new Functions(Map.of()), Locale.of("DE"), resourceAst);
        Message message = resource.message("date");
        System.out.println(message.interpolated(Map.of(
                "price", new Partial(12, Map.of("NUMBER", Map.of("style", "currency", "currency", "USD"))),
                "num", 12,
                "date", LocalDateTime.now()
                )
        ));
        System.out.println(message.apply(Map.of("date", ZonedDateTime.now())));
        System.out.println(message.interpolated(Map.of(
                        "date", ZonedDateTime.of(LocalDateTime.now(), ZoneId.of("America/Los_Angeles")))
                )
        );

    }
}
