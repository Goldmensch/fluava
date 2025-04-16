package dev.goldmensch.resource;

import dev.goldmensch.ast.FluentParser;
import dev.goldmensch.ast.tree.entry.Term;
import dev.goldmensch.message.Message;

import java.util.Map;
import java.util.stream.Collectors;

public class Resource {
    private final Map<String, Message> messages;
    private final Map<String, Message> terms;

    public Resource(dev.goldmensch.ast.tree.Resource resource) {
        this.terms = resource.components()
                .stream()
                .filter(Term.class::isInstance)
                .map(Term.class::cast)
                .collect(Collectors.toUnmodifiableMap(Term::id, term -> new Message(this, term)));

        this.messages = resource.components()
                .stream()
                .filter(dev.goldmensch.ast.tree.message.Message.class::isInstance)
                .map(dev.goldmensch.ast.tree.message.Message.class::cast)
                .collect(Collectors.toUnmodifiableMap(dev.goldmensch.ast.tree.message.Message::id, msg -> new Message(this, msg)));

    }

    public Message message(String key) {
        return messages.get(key);
    }

    public static void main(String[] args) {
        String text = """
# Simple things are simple.
hello-user = Hello, {$userName}!
    .attribute-one = test {$userName}

# Complex things are possible.
shared-photos =
    {$userName} {$photoCount ->
        [one] added a new photo
       *[other] added {$photoCount} new photos
    } to {$userGender ->
        [male] his stream
        [female] her stream
       *[other] their stream
    }

                """;

        dev.goldmensch.ast.tree.Resource resourceAst = new FluentParser().apply(text);
        Resource resource = new Resource(resourceAst);
        Message message = resource.message("hello-user");
        System.out.println(message.interpolated(Map.of("userName", "Nick")));

    }
}
