package dev.goldmensch.fluava;

import dev.goldmensch.fluava.ast.FluentParser;
import dev.goldmensch.fluava.ast.tree.AstResource;
import dev.goldmensch.fluava.function.internal.FunctionConfigImpl;
import dev.goldmensch.fluava.function.internal.Functions;

import java.util.List;
import java.util.Locale;

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
    private final FunctionConfigImpl functionConfig;

    /// @param fallback the fallback locale to use
    /// @param functionConfig the function configuration to use
    Fluava(Locale fallback, FunctionConfigImpl functionConfig) {
        this.fallback = fallback;
        this.functionConfig = functionConfig;
        this.functions = new Functions(functionConfig);
    }

    /// Creates a new [FluavaBuilder] to configure your [Fluava] instance.
    ///
    /// @return the [FluavaBuilder]
    public static FluavaBuilder builder() {
        return new FluavaBuilder();
    }

    /// Creates a new [FluavaBuilder] preset with the options from the provided [Fluava] instance.
    /// This can be used to crate "sub instances" which allows to override certain functions/add new ones.
    ///
    /// @param fluava the "parent" [Fluava] instance
    /// @return the [FluavaBuilder] preset with the options from the provided instance
    public static FluavaBuilder builder(Fluava fluava) {
        return new FluavaBuilder(fluava);
    }

    /// Creates a [Fluava] instance with the given fallback locale.
    ///
    /// @param fallback the fallback locale
    /// @return the configured [Fluava] instance
    public static Fluava create(Locale fallback) {
        return builder().fallback(fallback).build();
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

    // exposed for builder
    FunctionConfigImpl functionConfig() {
        return functionConfig;
    }

    Locale fallback() {
        return fallback;
    }
}
