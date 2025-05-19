package dev.goldmensch.fluava.message;

import dev.goldmensch.fluava.ast.tree.entry.Term;
import dev.goldmensch.fluava.ast.tree.message.Attribute;
import dev.goldmensch.fluava.function.Functions;
import dev.goldmensch.fluava.message.internal.Formatter;
import dev.goldmensch.fluava.resource.Resource;

import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class Message {

    private final Locale locale;
    private final Formatter contentFormatter;
    private final Map<String, Formatter> attributeFormatters;

    public Message(Functions functions, Locale locale, Resource leakingResource, dev.goldmensch.fluava.ast.tree.message.Message astMessage) {
        this.locale = locale;
        this.contentFormatter = astMessage.content()
                .map(msg -> new Formatter(functions, leakingResource, msg))
                .orElse(Formatter.EMPTY);

        this.attributeFormatters = astMessage.attributes()
                .stream()
                .collect(Collectors.toUnmodifiableMap(Attribute::id, (attribute) -> new Formatter(functions, leakingResource, attribute.pattern())));
    }

    public Message(Functions functions, Locale locale, Resource leakingResource, Term astTerm) {
        this.locale = locale;
        this.contentFormatter = new Formatter(functions, leakingResource, astTerm.pattern());
        this.attributeFormatters = astTerm.attributes()
                .stream()
                .collect(Collectors.toUnmodifiableMap(Attribute::id, (attribute) -> new Formatter(functions, leakingResource, attribute.pattern())));
    }

    public String apply(Map<String, Object> variables) {
        return contentFormatter.apply(locale, variables);
    }

    public Interpolated interpolated(Map<String, Object> variables) {
        Map<String, String> attributes = attributeFormatters.entrySet()
                .stream()
                .map(entry -> Map.entry(entry.getKey(), entry.getValue().apply(locale, variables)))
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));

        return new Interpolated(contentFormatter.apply(locale, variables), attributes);
    }

    public record Interpolated(
            String value,
            Map<String, String> attributes
    ) {}
}
