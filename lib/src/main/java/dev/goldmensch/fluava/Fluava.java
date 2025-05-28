package dev.goldmensch.fluava;

import dev.goldmensch.fluava.ast.FluentParser;
import dev.goldmensch.fluava.ast.tree.AstResource;
import dev.goldmensch.fluava.function.internal.Functions;
import dev.goldmensch.fluava.resource.Resource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;

public class Fluava {
    private final Functions functions = new Functions(Map.of());
    private final FluentParser parser = new FluentParser();

    public Result<Resource> of(String content, Locale locale) {
        Result<AstResource> parsingResult = parser.apply(content);

        return switch (parsingResult) {
            case Result.Success(AstResource value) -> new Result.Success<>(new Resource(functions, locale, value));
            case Result.Failure<?> failure -> failure.to();
        };
    }

    public Result<Resource> of(Path path, Locale locale) {
        try {
            String content = Files.readString(path);
            return of(content, locale);
        } catch (IOException e) {
            return new Result.Failure<>(e.getMessage());
        }
    }
}
