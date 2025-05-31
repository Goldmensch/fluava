package dev.goldmensch.fluava;

import dev.goldmensch.fluava.ast.tree.AstResource;
import dev.goldmensch.fluava.ast.tree.message.AstMessage;
import dev.goldmensch.fluava.function.internal.Functions;
import io.github.parseworks.FList;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Resource {

    public static final Resource EMPTY = new Resource(null, List.of());

    private final SequencedCollection<Level> levels;

    Resource(Functions functions, SequencedCollection<Map.Entry<Locale, AstResource>> source) {
        this.levels = source
                .stream()
                .map(entry -> {
                    FList<? extends AstResource.ResourceComponent> components = entry.getValue().components();
                    Locale locale = entry.getKey();

                    Map<String, Message> terms = components
                            .stream()
                            .filter(dev.goldmensch.fluava.ast.tree.entry.Term.class::isInstance)
                            .map(dev.goldmensch.fluava.ast.tree.entry.Term.class::cast)
                            .collect(Collectors.toUnmodifiableMap(dev.goldmensch.fluava.ast.tree.entry.Term::id, term -> new Message(functions, locale, this, term)));

                    Map<String, Message> messages = components
                            .stream()
                            .filter(AstMessage.class::isInstance)
                            .map(AstMessage.class::cast)
                            .collect(Collectors.toUnmodifiableMap(AstMessage::id, msg -> new Message(functions, locale, this, msg)));

                    return new Level(messages, terms);
                })
                .toList();
    }

    public Message message(String key) {
        return get(key, Level::messages);
    }

    public Message term(String key) {
        return get(key, Level::terms);
    }

    private Message get(String key, Function<Level, Map<String, Message>> mapper) {
        for (Level level : levels) {
            Map<String, Message> entries = mapper.apply(level);
            Message found = entries.get(key);
            if (found != null) return found;
        }

        return new Message(key);
    }

    private record Level(
            Map<String, Message> messages,
            Map<String, Message> terms
    ) {}
}
