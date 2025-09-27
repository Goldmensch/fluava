package dev.goldmensch.fluava;

import dev.goldmensch.fluava.ast.EntryP;
import dev.goldmensch.fluava.ast.FluentParser;
import dev.goldmensch.fluava.ast.tree.AstResource;
import dev.goldmensch.fluava.ast.tree.message.AstMessage;
import dev.goldmensch.fluava.ast.tree.message.Attribute;
import dev.goldmensch.fluava.ast.tree.pattern.Pattern;
import dev.goldmensch.fluava.function.Function;
import dev.goldmensch.fluava.function.internal.Functions;
import io.github.parseworks.FList;
import io.github.parseworks.Pair;
import io.github.parseworks.Parser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/// The main entrypoint for fluava.
///
/// To use this library, you have to create an instance of this class and pass a "fallback" locale,
/// which will be used if a certain key isn't found for a requested locale.
///
/// ## Example
/// ```java
/// // create Fluava instance with as English as the fallback locale
/// Fluava fluava = new Fluava(Locale.ENGLISH);
///
/// // get the bundle with basename app
/// Bundle appBundle = fluava.loadBundle("app");
///
/// // get message out of bundle
/// String failMessage = appBundle.apply(Locale.GERMAN, "fail", Map.of("msg", err.msg()));
/// ```
public class Fluava {
    private final Locale fallback;
    private final Functions functions;
    private final FluentParser parser = new FluentParser();

    /// @param fallback the fallback locale to use
    public Fluava(Locale fallback, Map<String, Function<?, ?>> functions) {
        this.fallback = fallback;
        this.functions = new Functions(functions);
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
