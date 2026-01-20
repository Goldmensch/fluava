package dev.goldmensch.fluava.test.parser.spec;

import dev.goldmensch.fluava.ast.FluentParser;
import dev.goldmensch.fluava.ast.tree.AstResource;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FixtureTestFactory {

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

            String fluentContent = Files.readString(file);
            String jsonContent = Files.readString(jsonFile);

            tests.add(buildTest(fluentContent, jsonContent, file.getFileName().toString()));
        }

        return tests;
    }

    private DynamicTest buildTest(String fluent, String json, String name) {
        return DynamicTest.dynamicTest("Test fixture %s".formatted(name), () -> {
            FluentParser parser = new FluentParser();
            AstResource resource = parser.apply(fluent).orElseThrow();// should never throw

            JSONObject parsed = new JsonFormatter().toJson(resource);
            JSONObject expected = new JSONObject(json);

            Assertions.assertEquals(expected, parsed);
        });
    }
}
