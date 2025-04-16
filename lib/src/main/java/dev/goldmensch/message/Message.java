package dev.goldmensch.message;

import dev.goldmensch.ast.FluentParser;
import dev.goldmensch.ast.tree.Resource;
import dev.goldmensch.ast.tree.message.Attribute;
import dev.goldmensch.message.internal.Formatter;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Message implements Function<Map<String, Object>, String> {

    private final Formatter contentFormatter;
    private final Map<String, Formatter> attributeFormatters;

    public Message(dev.goldmensch.ast.tree.message.Message astMessage) {
        this.contentFormatter = astMessage.content()
                .map(Formatter::new)
                .orElse(Formatter.EMPTY);

        this.attributeFormatters = astMessage.attributes()
                .stream()
                .collect(Collectors.toMap(Attribute::id, (attribute) -> new Formatter(attribute.pattern())));
    }

    @Override
    public String apply(Map<String, Object> variables) {
        return contentFormatter.apply(variables);
    }

    public Interpolated interpolated(Map<String, Object> variables) {
        Map<String, String> attributes = attributeFormatters.entrySet()
                .stream()
                .map(entry -> Map.entry(entry.getKey(), entry.getValue().apply(variables)))
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));

        return new Interpolated(contentFormatter.apply(variables), attributes);
    }

    public record Interpolated(
            String value,
            Map<String, String> attributes
    ) {}

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

        Resource res = new FluentParser().apply(text);
        for (Resource.ResourceComponent entry : res.components()) {
            if (entry instanceof dev.goldmensch.ast.tree.message.Message message) {
                Message msg = new Message(message);
                Interpolated result = msg.interpolated(Map.of("userName", "Nick"));
//                System.out.println(message.content());
                System.out.println(result);
            }
        }
    }
}
