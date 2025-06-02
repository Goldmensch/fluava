package dev.goldmensch.fluava;

import dev.goldmensch.fluava.ast.tree.entry.Term;
import dev.goldmensch.fluava.ast.tree.message.AstMessage;
import dev.goldmensch.fluava.ast.tree.message.Attribute;
import dev.goldmensch.fluava.function.internal.Functions;
import dev.goldmensch.fluava.internal.Formatter;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/// This class represents a message or term identified by a specific key in a fluent file.
/// Each message instance represents exactly one message/term for one specific locale.
///
/// All methods of this class are guaranteed to never throw an exception or return null.
/// If a message isn't found, methods of this class will return the key instead.
public class Message {

    private final String key;
    private final Locale locale;
    private final Formatter contentFormatter;
    private final Map<String, Formatter> attributeFormatters;

    Message(String key) {
        this.key = key;
        this.locale = null;
        this.contentFormatter = null;
        this.attributeFormatters = null;
    }

    Message(Functions functions, Locale locale, Resource leakingResource, AstMessage astMessage) {
        this.key = astMessage.id();
        this.locale = Objects.requireNonNull(locale);
        this.contentFormatter = astMessage.content()
                .map(msg -> new Formatter(functions, leakingResource, msg))
                .orElse(Formatter.EMPTY);

        this.attributeFormatters = astMessage.attributes()
                .stream()
                .collect(Collectors.toUnmodifiableMap(Attribute::id, (attribute) -> new Formatter(functions, leakingResource, attribute.pattern())));
    }

    Message(Functions functions, Locale locale, Resource leakingResource, Term astTerm) {
        this.key = astTerm.id();
        this.locale = Objects.requireNonNull(locale);
        this.contentFormatter = new Formatter(functions, leakingResource, astTerm.pattern());
        this.attributeFormatters = astTerm.attributes()
                .stream()
                .collect(Collectors.toUnmodifiableMap(Attribute::id, (attribute) -> new Formatter(functions, leakingResource, attribute.pattern())));
    }

    /// Returns the message after applying the given variables.
    ///
    /// @param variables the variables and their value
    /// @return the formatted message
    public String apply(Map<String, Object> variables) {
        if (notFound()) return key;

        return contentFormatter.apply(locale, variables);
    }

    /// Returns both message and attributes of this entry after applying the given variables.
    ///
    /// @param variables the variables and their value
    /// @return the formatted message and attributes
    public Interpolated interpolated(Map<String, Object> variables) {
        if (notFound()) return new Interpolated(key, Map.of());

        Map<String, String> attributes = attributeFormatters.entrySet()
                .stream()
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, entry -> entry.getValue().apply(locale, variables)));

        return new Interpolated(contentFormatter.apply(locale, variables), attributes);
    }

    private boolean notFound() {
        return locale == null; // only null if message not found
    }

    public class Interpolated {

        private final String value;
        private final Map<String, String> attributes;

        private Interpolated(String value, Map<String, String> attributes) {
            this.value = Objects.requireNonNull(value);
            this.attributes = Objects.requireNonNull(attributes);
        }

        /// @return the formatted message
        public String value() {
            return value;
        }

        /// @param attKey the attributes key
        /// @return the formatted attribute of this message
        public String attribute(String attKey) {
            return attributes.getOrDefault(attKey, key + "#" + attKey);
        }

        @Override
        public String toString() {
            return "Interpolated{" +
                    "value='" + value + '\'' +
                    ", attributes=" + attributes +
                    '}';
        }
    }
}
