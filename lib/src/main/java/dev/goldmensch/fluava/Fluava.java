package dev.goldmensch.fluava;

import dev.goldmensch.fluava.ast.FluentParser;
import dev.goldmensch.fluava.ast.tree.AstResource;
import dev.goldmensch.fluava.function.internal.Functions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Fluava {
    private final Locale fallback;
    private final Functions functions = new Functions(Map.of());
    private final FluentParser parser = new FluentParser();

    public Fluava(Locale fallback) {
        this.fallback = fallback;
    }

    public Result<Resource> of(String content, Locale locale) {
        Result<AstResource> parsingResult = parse(content);

        return switch (parsingResult) {
            case Result.Success(AstResource value) -> new Result.Success<>(new Resource(functions, List.of(Map.entry(locale, value))));

            case Result.Failure<?> failure -> failure.to();
        };
    }

    public Result<Resource> loadResource(Path path, Locale locale) {
        try {
            String content = Files.readString(path);
            return of(content, locale);
        } catch (IOException e) {
            return new Result.Failure<>(e.getMessage());
        }
    }

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
