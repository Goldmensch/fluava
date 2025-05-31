package dev.goldmensch.fluava;

import dev.goldmensch.fluava.ast.tree.AstResource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Bundle {
    private final Fluava fluava;
    private final Locale fallback;
    private final String base;
    private final ConcurrentHashMap<Locale, Resource> loadedResources = new ConcurrentHashMap<>();

    public Bundle(Fluava fluava, Locale fallback, String base) {
        this.fluava = fluava;
        this.fallback = fallback;
        this.base = base;
    }

    public Message message(Locale locale, String key) {
        return getResource(locale).message(key);
    }

    public String apply(Locale locale, String key, Map<String, Object> variables) {
        return message(locale, key).apply(variables);
    }

    public Message.Interpolated interpolated(Locale locale, String key, Map<String, Object> variables) {
        return message(locale, key).interpolated(variables);
    }

    private Resource getResource(Locale locale) {
        return loadedResources.computeIfAbsent(locale, this::load);
    }

    private Resource load(Locale locale) {
        SequencedCollection<Map.Entry<Locale, AstResource>> sources = loadSources(locale);

        if (sources.isEmpty() && locale == fallback) return Resource.EMPTY;
        if (locale != fallback) {
            sources.addAll(loadSources(fallback));
        }

        return new Resource(fluava.functions(), sources);
    }

    private SequencedCollection<Map.Entry<Locale, AstResource>> loadSources(Locale l) {
        List<Map.Entry<Locale, String>> variants = List.of(
                Map.entry(l, "%s_%s_%s.ftl".formatted(l.getLanguage(), l.getCountry(), l.getVariant())),
                Map.entry(Locale.of(l.getLanguage(), l.getCountry()), "%s_%s.ftl".formatted(l.getLanguage(), l.getCountry())),
                Map.entry(Locale.of(l.getLanguage()), "%s.ftl".formatted(l.getLanguage()))
        );

        SequencedCollection<Map.Entry<Locale, AstResource>> sources = new ArrayList<>();
        variants.forEach(entry -> {
            try {
                String name = entry.getValue();
                Locale locale = entry.getKey();

                AstResource resource = loadFound(find(base + "_", name));
                if (resource == null) resource = loadFound(find(base + "/", name));
                if (resource != null) sources.add(Map.entry(locale, resource));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        return sources;
    }

    private String find(String prefix, String name) throws IOException {
        String path = "/%s%s".formatted(prefix, name);
        try (InputStream in = this.getClass().getResourceAsStream(path)) {
            if (in == null) return null;
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private AstResource loadFound(String content) {
        if (content == null) return null;
        return fluava.parse(content).orElse(null);
    }


}
