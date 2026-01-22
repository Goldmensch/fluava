package dev.goldmensch.fluava;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/// A bundle is a collection of several fluent resources for multiple locales.
/// The fluent files are searched lazily, similar to how [ResourceBundle] work.
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
/// The parts correspond to the [lower case][String#toLowerCase()] return type of following methods:
///
/// - LANGUAGE -> [Locale#getLanguage()]
/// - COUNTRY -> [Locale#getCountry()]
/// - VARIANT -> [Locale#getVariant()]
///
/// If a key isn't found in any of the above files, the same procedure will be done for the given "fallback" locale.
/// If even then a key isn't found, the key will be returned as the "translated value" by any [Resource]/[Message].
///
/// You can set a root package that [Bundle]s should be loaded from with [FluavaBuilder#bundleRoot(String)].
/// The bundle's classpath path will be prefixed with it.
/// For example, if the root path is "localization", a bundle named "msg" will be loaded from "localization/msg".
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

    /// Searches this bundle for the specified message.
    /// If the key contains an attribute, the attribute is returned as the message. (see [Resource#message(String)])
    ///
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
    /// If the key contains an attribute, the attribute is returned as the message. (see [Resource#message(String)])
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

        log.debug("Loading sources of bundle with base {} for locale {}", base, l);

        String language = l.getLanguage().toLowerCase();
        String country = l.getCountry().toLowerCase();
        String variant = l.getVariant().toLowerCase();
        return Stream.of(
                        new Bundle.Pair(l, "%s_%s_%s.ftl".formatted(language, country, variant)),
                        new Bundle.Pair(Locale.of(language, country), "%s_%s.ftl".formatted(language, country)),
                        new Bundle.Pair(Locale.of(language), "%s.ftl".formatted(language))
                )
                .flatMap(pair -> readFile(base + "_", pair.name, pair.locale)
                        .or(() -> readFile(base + "/", pair.name, pair.locale)).stream())
                .peek(triple -> log.debug("Found fluent file {} for bundle {} and locale {}", triple.name, base, triple.resource.locale()))
                .map(Triple::resource)
                .collect(Collectors.toList());
    }

    private record Pair(Locale locale, String name) {}
    private record Triple(String name, Resource.Pair resource) {}

    private Optional<Triple> readFile(String prefix, String name, Locale locale) {
        String path = "/%s%s".formatted(prefix, name);

        log.debug("Trying to read file from classpath: {}", path);

        try (InputStream in = this.getClass().getResourceAsStream(path)) {
            if (in == null) return Optional.empty();
            String content = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            return fluava.parse(content)
                    .toOptional()
                    .map(resource -> new Triple(name, new Resource.Pair(locale, resource)));
        } catch (IOException e) {
            log.error("Error while reading fluent file from classpath at {}", path, e);
            return Optional.empty();
        }
    }


}
