package dev.goldmensch.fluava.resource;

import dev.goldmensch.fluava.ast.FluentParser;
import dev.goldmensch.fluava.ast.tree.entry.Term;
import dev.goldmensch.fluava.ast.tree.message.Message;
import dev.goldmensch.fluava.function.Functions;
import dev.goldmensch.fluava.function.Partial;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class Resource {
    private final Map<String, dev.goldmensch.fluava.message.Message> messages;
    private final Map<String, dev.goldmensch.fluava.message.Message> terms;

    public Resource(Functions functions, Locale locale, dev.goldmensch.fluava.ast.tree.Resource resource) {
        this.terms = resource.components()
                .stream()
                .filter(Term.class::isInstance)
                .map(Term.class::cast)
                .collect(Collectors.toUnmodifiableMap(Term::id, term -> new dev.goldmensch.fluava.message.Message(functions, locale, this, term)));

        this.messages = resource.components()
                .stream()
                .filter(Message.class::isInstance)
                .map(Message.class::cast)
                .collect(Collectors.toUnmodifiableMap(Message::id, msg -> new dev.goldmensch.fluava.message.Message(functions, locale, this, msg)));
    }

    public dev.goldmensch.fluava.message.Message message(String key) {
        return messages.get(key);
    }

    public dev.goldmensch.fluava.message.Message term(String key) {
        return terms.get(key);
    }

    public static void main(String[] args) {
        String text = """
                info = The price is { NUMBER($price, currencyDisplay: "name") }
                test = The number is { $num }
                date = Today is { DATETIME($date, hour12: "false", weekday: "short", timeZoneName: "short") }
                """;

        dev.goldmensch.fluava.ast.tree.Resource resourceAst = new FluentParser().apply(text);
        Resource resource = new Resource(new Functions(Map.of()), Locale.of("DE"), resourceAst);
        dev.goldmensch.fluava.message.Message message = resource.message("info");
        System.out.println(message.interpolated(Map.of(
                "price", new Partial(12, Map.of("NUMBER", Map.of("style", "currency", "currency", "USD"))),
                "num", 12,
                "date", LocalDateTime.now()
                )
        ));

    }
}
