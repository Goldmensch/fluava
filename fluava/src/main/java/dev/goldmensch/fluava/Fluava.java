package dev.goldmensch.fluava;

import dev.goldmensch.fluava.ast.FluentParser;
import dev.goldmensch.fluava.ast.tree.AstResource;
import dev.goldmensch.fluava.function.internal.Functions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/// The main entrypoint for fluava.
///
/// To use this library, you have to create an instance of this class and pass a "fallback" locale,
/// which will be used if a certain key isn't found for a requested locale.
public class Fluava {
    private final Locale fallback;
    private final Functions functions = new Functions(Map.of());
    private final FluentParser parser = new FluentParser();

    /// @param fallback the fallback locale to use
    public Fluava(Locale fallback) {
        this.fallback = fallback;
    }

    /// Creates a new [Resource] from the given contents and locale.
    ///
    /// @param content the fluent file content
    /// @param locale the corresponding locale
    /// @return the newly created resource
    public Result<Resource> of(String content, Locale locale) {
        Result<AstResource> parsingResult = parse(content);

        return switch (parsingResult) {
            case Result.Success(AstResource value) -> new Result.Success<>(new Resource(functions, List.of(new Resource.Pair(locale, value))));
            case Result.Failure<?> failure -> failure.to();
        };
    }

    /// Reads in the given file and creates a new [Resource] according to [#of(String, Locale)]
    ///
    /// @param path the fluent file to be read
    /// @param locale the corresponding locale
    /// @return the newly created resource
    public Result<Resource> loadResource(Path path, Locale locale) {
        try {
            String content = Files.readString(path);
            return of(content, locale);
        } catch (IOException e) {
            return new Result.Failure<>(e.getMessage());
        }
    }

    /// Creates a new [Bundle] with the given base path
    ///
    /// @return the newly creates [Bundle]
    public Bundle loadBundle(String base) {
        return new Bundle(this, fallback, base);
    }

    Result<AstResource> parse(String content) {
        return parser.apply(content);
    }

    Functions functions() {
        return functions;
    }
}
