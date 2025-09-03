package dev.goldmensch.fluava;

import dev.goldmensch.fluava.ast.tree.AstResource;
import dev.goldmensch.fluava.ast.tree.message.AstMessage;
import dev.goldmensch.fluava.function.internal.Functions;
import io.github.parseworks.FList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/// A resource is a layered representation of one or more fluent files.
///
/// Every method of this class is guaranteed to never throw an exception or return null.
/// If a key looked for isn't found, then an empty message is returned which just return the messages key.
public class Resource {

    private static final Logger log = LoggerFactory.getLogger(Resource.class);
    private final SequencedCollection<Level> levels;

    Resource(Functions functions, SequencedCollection<Pair> source) {
        this.levels = source
                .stream()
                .map(entry -> {
                    FList<? extends AstResource.ResourceComponent> components = entry.resource().components();

                    Map<String, Message> terms = components
                            .stream()
                            .filter(dev.goldmensch.fluava.ast.tree.entry.Term.class::isInstance)
                            .map(dev.goldmensch.fluava.ast.tree.entry.Term.class::cast)
                            .collect(Collectors.toUnmodifiableMap(dev.goldmensch.fluava.ast.tree.entry.Term::id, term -> new Message(functions, entry.locale, this, term)));

                    Map<String, Message> messages = components
                            .stream()
                            .filter(AstMessage.class::isInstance)
                            .map(AstMessage.class::cast)
                            .collect(Collectors.toUnmodifiableMap(AstMessage::id, msg -> new Message(functions, entry.locale, this, msg)));

                    return new Level(messages, terms, entry.locale);
                })
                .toList();
    }

    record Pair(Locale locale, AstResource resource) {}

    /// @param key the message key
    /// @return the found message
    public Message message(String key) {
        return get(key, Level::messages);
    }

    /// @param key the term key
    /// @return the found term
    public Message term(String key) {
        return get(key, Level::terms);
    }

    /// @return the locales from what this [Resource] will load its messages.
    public Set<Locale> locales() {
        return levels.stream().map(Level::locale).collect(Collectors.toUnmodifiableSet());
    }

    /// @return whether this [Resource] is empty
    public boolean isEmpty() {
        return locales().isEmpty();
    }

    private Message get(String key, Function<Level, Map<String, Message>> mapper) {
        for (Level level : levels) {
            Map<String, Message> entries = mapper.apply(level);
            Message found = entries.get(key);
            if (found != null) return found;
        }

        String searchedLocals = levels.stream()
                .map(Level::locale)
                .map(Locale::toString)
                .collect(Collectors.joining(", "));
        log.warn("Didn't find key '{}', searched in locals: {}", key, searchedLocals);
        return new Message(key);
    }

    private record Level(
            Map<String, Message> messages,
            Map<String, Message> terms,
            Locale locale
    ) {}
}
