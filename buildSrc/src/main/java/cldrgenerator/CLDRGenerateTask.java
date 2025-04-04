package cldrgenerator;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
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
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CLDRGenerateTask extends DefaultTask {

    private static final int SUPPORTED_CLDR_VERSION = 47;

    private static final URI CARDINAL_URL = URI.create("https://raw.githubusercontent.com/unicode-org/cldr-json/refs/heads/main/cldr-json/cldr-core/supplemental/plurals.json");
    private static final URI ORDINAL_URL = URI.create("https://raw.githubusercontent.com/unicode-org/cldr-json/refs/heads/main/cldr-json/cldr-core/supplemental/ordinals.json");

    Path output = getProject().getLayout().getBuildDirectory().dir("generated/cldr").get().getAsFile().toPath();

    @Inject
    public CLDRGenerateTask() {
        SourceSetContainer sourceSets = getProject().getExtensions().getByType(SourceSetContainer.class);
        sourceSets.getByName("main").getJava().srcDir(output);

        getProject().delete(output);
    }

    @TaskAction
    public void generate() {
        Map<Locale, Map<String, String>> cardinal = downloadJson(CARDINAL_URL, content -> parse(content, "cardinal"));
        Map<Locale, Map<String, String>> ordinal = downloadJson(ORDINAL_URL, content -> parse(content, "ordinal"));

        cardinal.forEach((locale, stringStringMap) -> System.out.println(locale.getDisplayLanguage() + " -> " + stringStringMap));

        System.out.println();

        ordinal.forEach((locale, stringStringMap) -> System.out.println(locale.getDisplayLanguage() + " -> " + stringStringMap));
    }

    @OutputDirectory
    public Path getOutput() {
        return output;
    }

    private Map<Locale, Map<String, String>> parse(JSONObject json, String name) {
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

    private Map<String, String> parseRules(JSONObject object) {
        return object.keySet().stream()
                .collect(Collectors.toMap(
                        s -> s.replaceFirst("pluralRule-count-", "").toUpperCase(),
                        object::getString)
                );
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
