package dev.goldmensch.fluava.resource;

import dev.goldmensch.fluava.function.Functions;
import dev.goldmensch.fluava.message.Message;

import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class Resource {
    private final Map<String, Message> messages;
    private final Map<String, Message> terms;

    public Resource(Functions functions, Locale locale, dev.goldmensch.fluava.ast.tree.Resource resource) {
        this.terms = resource.components()
                .stream()
                .filter(dev.goldmensch.fluava.ast.tree.entry.Term.class::isInstance)
                .map(dev.goldmensch.fluava.ast.tree.entry.Term.class::cast)
                .collect(Collectors.toUnmodifiableMap(dev.goldmensch.fluava.ast.tree.entry.Term::id, term -> new Message(functions, locale, this, term)));

        this.messages = resource.components()
                .stream()
                .filter(dev.goldmensch.fluava.ast.tree.message.Message.class::isInstance)
                .map(dev.goldmensch.fluava.ast.tree.message.Message.class::cast)
                .collect(Collectors.toUnmodifiableMap(dev.goldmensch.fluava.ast.tree.message.Message::id, msg -> new Message(functions, locale, this, msg)));
    }

    public Message message(String key) {
        return get(key, messages);
    }

    public Message term(String key) {
        return get(key, terms);
    }

    private Message get(String key, Map<String, Message> map) {
        Message message = map.get(key);
        if (message == null) return new Message(key);
        return message;
    }
}
