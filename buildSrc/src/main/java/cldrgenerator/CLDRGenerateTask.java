package cldrgenerator;

import cldrgenerator.ast.Condition;
import com.palantir.javapoet.JavaFile;
import io.github.parseworks.Result;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.Directory;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskAction;
import org.json.JSONObject;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CLDRGenerateTask extends DefaultTask {

    private static final int SUPPORTED_CLDR_VERSION = 47;

    private static final URI CARDINAL_URL = URI.create("https://raw.githubusercontent.com/unicode-org/cldr-json/refs/heads/main/cldr-json/cldr-core/supplemental/plurals.json");
    private static final URI ORDINAL_URL = URI.create("https://raw.githubusercontent.com/unicode-org/cldr-json/refs/heads/main/cldr-json/cldr-core/supplemental/ordinals.json");

    Directory output = getProject().getLayout().getBuildDirectory().dir("generated/cldr").get();
    Directory sourceOutput = output.dir("source");
    Directory resourceOutput = output.dir("resources");

    @Inject
    public CLDRGenerateTask() {
        SourceSetContainer sourceSets = getProject().getExtensions().getByType(SourceSetContainer.class);
        sourceSets.getByName("main").getJava().srcDir(sourceOutput);
        sourceSets.getByName("main").getResources().srcDir(resourceOutput);

        getProject().delete(output);
    }

    @TaskAction
    public void generate() {
        Map<Locale, Map<String, Condition>> cardinal = downloadJson(CARDINAL_URL, content -> parse(content, "cardinal"));
        Map<Locale, Map<String, Condition>> ordinal = downloadJson(ORDINAL_URL, content -> parse(content, "ordinal"));


        Generator generator = new Generator();
        JavaFile file = generator.generate(cardinal, ordinal);

        try {
            file.writeToFile(sourceOutput.getAsFile());

            Path servicesFile = resourceOutput.getAsFile().toPath().resolve(Path.of("META-INF", "services", Generator.CLDR_PACKAGE + ".RulesProvider"));
            Files.createDirectories(servicesFile.getParent());
            Files.writeString(servicesFile, file.packageName() + "." + Generator.CLASS_NAME, StandardOpenOption.CREATE_NEW);
        } catch (IOException e) {
            throw new GradleException("Failed to write generated files to %s".formatted(output), e);
        }
    }

    @OutputDirectory
    public Directory getOutput() {
        return output;
    }

    private Map<Locale, Map<String, Condition>> parse(JSONObject json, String name) {
        JSONObject supplemental = json.getJSONObject("supplemental");
        JSONObject version = supplemental.getJSONObject("version");

        if (version.getInt("_cldrVersion") != SUPPORTED_CLDR_VERSION) {
            throw new GradleException("Unsupported cldr version %s".formatted(version.getInt("_cldrVersion")));

        }

        JSONObject values = supplemental.getJSONObject("plurals-type-" + name);
        return values.keySet()
                .stream()
                .collect(Collectors.toMap(Locale::of, s -> parseRules(values.getJSONObject(s))));
    }

    private Map<String, Condition> parseRules(JSONObject object) {
        return object.keySet().stream()
                .collect(Collectors.toMap(
                        s -> s.replaceFirst("pluralRule-count-", "").toUpperCase(),
                        key -> parseCondition(object.getString(key)))
                );
    }

    private Condition parseCondition(String s) {
        // strip pattern whitespace
        int[] chars = s.chars()
                .filter(value -> !List.of(0x200E, 0x200F, 0x0085, 0x2028, 0x2029, 0x0020).contains(value)
                        && !(0x009 <= value && value <= 0x00D))
                .toArray();
        String strippedString = new String(chars, 0, chars.length);

        Result<Character, Condition> result = Parser.rule.parse(strippedString);
        return result.get();
    }

    private <T> T downloadJson(URI uri, Function<JSONObject, T> mapper) {
        HttpResponse<String> response;
        try (HttpClient client = HttpClient.newHttpClient()) {
            response = client.send(HttpRequest.newBuilder(uri).build(), HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new GradleException("Failed to retrieve content from %s".formatted(uri));
        }

        String content = response.body();

        return mapper.apply(new JSONObject(content));
    }
}
