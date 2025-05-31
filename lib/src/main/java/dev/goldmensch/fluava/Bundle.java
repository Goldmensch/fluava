package dev.goldmensch.fluava;

import dev.goldmensch.fluava.ast.tree.AstResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.SequencedCollection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Bundle {
    public static final Logger log = LoggerFactory.getLogger(Bundle.class);

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
        SequencedCollection<Resource.Pair> sources = loadSources(locale);

        if (locale != fallback) {
            sources.addAll(loadSources(fallback));
        }

        return new Resource(fluava.functions(), sources);
    }

    private SequencedCollection<Resource.Pair> loadSources(Locale l) {
        return Stream.of(
                        new Bundle.Pair(l, "%s_%s_%s.ftl".formatted(l.getLanguage(), l.getCountry(), l.getVariant())),
                        new Bundle.Pair(Locale.of(l.getLanguage(), l.getCountry()), "%s_%s.ftl".formatted(l.getLanguage(), l.getCountry())),
                        new Bundle.Pair(Locale.of(l.getLanguage()), "%s.ftl".formatted(l.getLanguage()))
                )
                .map(pair -> {
                    AstResource resource = readFile(base + "_", pair.name);
                    if (resource == null) resource = readFile(base + "/", pair.name);
                    if (resource != null) return new Resource.Pair(pair.locale, resource);
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private record Pair(Locale locale, String name) {}

    private AstResource readFile(String prefix, String name) {
        String path = "/%s%s".formatted(prefix, name);
        try (InputStream in = this.getClass().getResourceAsStream(path)) {
            if (in == null) return null;
            String content = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            return fluava.parse(content).orElse(null);
        } catch (IOException e) {
            log.error("Error while reading fluent file from classpath at {}", path, e);
            return null;
        }
    }


}
