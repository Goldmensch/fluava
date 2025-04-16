package dev.goldmensch.message;

import dev.goldmensch.ast.tree.entry.Term;
import dev.goldmensch.ast.tree.message.Attribute;
import dev.goldmensch.message.internal.Formatter;
import dev.goldmensch.resource.Resource;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Message implements Function<Map<String, Object>, String> {

    private final Formatter contentFormatter;
    private final Map<String, Formatter> attributeFormatters;

    public Message(Resource leakingResource, dev.goldmensch.ast.tree.message.Message astMessage) {
        this.contentFormatter = astMessage.content()
                .map(msg -> new Formatter(leakingResource, msg))
                .orElse(Formatter.EMPTY);

        this.attributeFormatters = astMessage.attributes()
                .stream()
                .collect(Collectors.toUnmodifiableMap(Attribute::id, (attribute) -> new Formatter(leakingResource, attribute.pattern())));
    }

    public Message(Resource leakingResource, Term astTerm) {
        this.contentFormatter = new Formatter(leakingResource, astTerm.pattern());
        this.attributeFormatters = astTerm.attributes()
                .stream()
                .collect(Collectors.toUnmodifiableMap(Attribute::id, (attribute) -> new Formatter(leakingResource, attribute.pattern())));
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
}
