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

/// A bundle is a collection of several fluent resources for multiple locales.
/// The fluent files are searched lazily, similar to how [java.util.ResourceBundle] work.
///
/// The classpath will be searched for a fluent file given a specific locale according to following order:
///
/// 1. BASE_LANGUAGE_COUNTRY_VARIANT.ftl
/// 2. BASE/LANGUAGE_COUNTRY_VARIANT.ftl
/// 3. BASE_LANGUAGE_COUNTRY.ftl
/// 4. BASE/LANGUAGE_COUNTRY.ftl
/// 5. BASE_LANGUAGE.ftl
/// 6. BASE/LANGUAGE.ftl
///
/// If a key isn't found in any of the above files, the same procedure will be done for the given "fallback" locale.
/// If even then a key isn't found, the key will be returned as the "translated value" by any [Resource]/[Message].
///
public class Bundle {
    public static final Logger log = LoggerFactory.getLogger(Bundle.class);

    private final Fluava fluava;
    private final Locale fallback;
    private final String base;
    private final ConcurrentHashMap<Locale, Resource> loadedResources = new ConcurrentHashMap<>();

    Bundle(Fluava fluava, Locale fallback, String base) {
        this.fluava = fluava;
        this.fallback = fallback;
        this.base = base;
    }

    /// @param key the key of the searched message
    /// @param locale the locale to be searched for
    ///
    /// @return the found [Message]
    public Message message(Locale locale, String key) {
        return resource(locale).message(key);
    }

    /// Searches the bundle for the specific key and locale according to the rules stated above.
    /// If found, the specified variables will be applied to the placeable in the message.
    ///
    /// @param key the key of the searched message
    /// @param locale the locale to be searched for
    /// @param variables the variables, which should be applied on the found message
    ///
    /// @return the formatted message
    public String apply(Locale locale, String key, Map<String, Object> variables) {
        return message(locale, key).apply(variables);
    }

    /// Searches the bundle for the specific key and locale according to the rules stated above.
    /// If found, the specified variables will be applied to the placeable in the message.
    ///
    /// @param key the key of the searched message
    /// @param locale the locale to be searched for
    /// @param variables the variables, which should be applied on the found message
    ///
    /// @return the formatted message and attributes of the message
    public Message.Interpolated interpolated(Locale locale, String key, Map<String, Object> variables) {
        return message(locale, key).interpolated(variables);
    }

    /// Searched for the best fit fluent file in this bundle. If not found, this method returns
    /// an "empty" [Resource] that always returns the key as the value.
    ///
    /// @param locale the requested locale
    ///
    /// @return the found [Resource]
    public Resource resource(Locale locale) {
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
