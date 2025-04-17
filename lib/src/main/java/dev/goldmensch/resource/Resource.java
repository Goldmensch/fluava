package dev.goldmensch.resource;

import dev.goldmensch.ast.FluentParser;
import dev.goldmensch.ast.tree.entry.Term;
import dev.goldmensch.function.Functions;
import dev.goldmensch.message.Message;

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
-term = Hi!
    .att1 = hehe { NUMBER($val, test: "hehe") }

# Simple things are simple.
hello-user = Hello, {$userName}!
    .attribute-one = test {$userName} term: { -term.att1(val: 123232) }

# Complex things are possible.
shared-photos = refmsg: { hello-user.attribute-one }
    {$userName} {$photoCount ->
        [one] added a new photo
        [few] added two new photos
        [4] add four new photos
        *[other] added {$photoCount} new photos
    } to {$userGender ->
        [male] his stream
        [female] her stream
       *[other] their stream
    }

                """;

        dev.goldmensch.ast.tree.Resource resourceAst = new FluentParser().apply(text);
        Resource resource = new Resource(new Functions(Map.of()), Locale.of("uk"), resourceAst);
        Message message = resource.message("shared-photos");
        System.out.println(message.interpolated(Map.of("userName", "Nick", "photoCount", 34, "userGender", "male")));

    }
}
