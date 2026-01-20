package dev.goldmensch.fluava.test.parser.spec;

import dev.goldmensch.fluava.ast.FluentParser;
import dev.goldmensch.fluava.ast.tree.AstResource;
import org.json.JSONObject;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FixtureTestFactory {

    static void main() {
        String fluentContent = """
                key08 =     Leading and trailing whitespace.    \s
                """;

        FluentParser parser = new FluentParser();
        AstResource resource = parser.apply(fluentContent).orElseThrow();// should never throw
        System.out.println(resource);
    }

    @TestFactory
    @DisplayName("Fixtures tests")
    @SuppressWarnings("resource")
    Collection<DynamicTest> createTest() throws IOException {
        Path dir = Path.of("src/test/resources/fixtures");
        List<Path> fluentFiles = Files.walk(dir)
                .filter(path -> path.getFileName().toString().endsWith(".ftl"))
                .toList();


        List<DynamicTest> tests = new ArrayList<>();
        for (Path file : fluentFiles) {
            String jsonName = file.getFileName().toString().replace(".ftl", ".json");
            Path jsonFile = file.getParent().resolve(jsonName);

            tests.add(buildTest(file, jsonFile));
        }

        return tests;
    }

    private DynamicTest buildTest(Path fluent, Path json) throws IOException {

        String name = fluent.getFileName().toString();
        String fluentContent = Files.readString(fluent);
        String jsonContent = Files.readString(json);

        return DynamicTest.dynamicTest("Test fixture %s".formatted(name), () -> {
            System.out.println(name);
            FluentParser parser = new FluentParser();
            AstResource resource = parser.apply(fluentContent).orElseThrow();// should never throw

            JSONObject parsed = new JsonFormatter().toJson(resource);
            JSONObject expected = new JSONObject(jsonContent);

            if (!expected.similar(parsed)) {
                AssertionFailureBuilder.assertionFailure()
                        .expected(expected.toString(2))
                        .actual(parsed.toString(2))
                        .message("""
                                Error while checking fixture: %s
                                
                                Fluent file: %s
                                Json file: %s
                                """.formatted(name, fluent.toAbsolutePath().toUri(), json.toAbsolutePath().toUri()))
                        .buildAndThrow();
            }
        });
    }
}
