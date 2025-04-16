package dev.goldmensch.message.internal;

import dev.goldmensch.ast.tree.expression.Variant;
import dev.goldmensch.cldrplurals.PluralCategory;
import dev.goldmensch.cldrplurals.Plurals;
import dev.goldmensch.cldrplurals.Type;
import dev.goldmensch.message.Message;
import dev.goldmensch.resource.Resource;
import dev.goldmensch.ast.tree.expression.Argument;
import dev.goldmensch.ast.tree.expression.InlineExpression;
import dev.goldmensch.ast.tree.expression.SelectExpression;
import dev.goldmensch.ast.tree.pattern.Pattern;
import dev.goldmensch.ast.tree.pattern.PatternElement;
import io.github.parseworks.FList;

import java.util.*;
import java.util.stream.Collectors;

public class Formatter {
    public static final Formatter EMPTY = new Formatter();

    private final FList<PatternElement> components;
    private final Resource resource;

    private Formatter() {
        this.components = null;
        this.resource = null;
    }

    public Formatter(Resource resource, Pattern ast) {
        Objects.requireNonNull(ast);
        Objects.requireNonNull(resource);

        this.components = ast.components();
        this.resource = resource;
    }

    public String apply(Locale locale, Map<String, Object> variables) {
        if (components == null) return "";

        Task task = new Task(locale, new StringBuilder(), variables);

        for (PatternElement component : components) {
            add(task, component);
        }

        return task.builder().toString();
    }

    private void add(Task task, PatternElement element) {
        switch (element) {
            case PatternElement.Text(String val) -> task.append(val);
            case PatternElement.Placeable placeable -> addPlaceable(task, placeable);
        }
    }

    private void addPlaceable(Task task, PatternElement.Placeable placeable) {
        switch (placeable) {
            case SelectExpression select -> addSelect(task, select);
            case InlineExpression inline -> addInlineExpression(task, inline);
        }
    }

    private void addSelect(Task task, SelectExpression expression) {
        Computed selector = computeExpression(task, expression.expression());

        switch (selector) {
            case Computed.Text(String computedValue) -> {
                for (Variant entry : expression.others()) {
                    if (entry.key() instanceof InlineExpression.StringLiteral(String value) && computedValue.equals(value)) {
                        addPattern(task, entry.pattern());
                        return;
                    }
                }
            }

            case Computed.Number(String computedValue, double original) -> {
                PluralCategory category = Plurals.find(task.locale(), Type.CARDINAL, computedValue);

                for (Variant entry : expression.others()) {
                    if ((entry.key() instanceof InlineExpression.NumberLiteral(double number) && original == number) ||
                            (entry.key() instanceof InlineExpression.StringLiteral(
                                    String text
                            ) && PluralCategory.valueOf(text.toUpperCase()) == category)
                    ) {
                        addPattern(task, entry.pattern());
                        return;
                    }
                }
            }
        }

        addPattern(task, expression.defaultVariant());
    }

    private void addPattern(Task task, Pattern pattern) {
        String formatted = new Formatter(resource, pattern).apply(task.locale(), task.variables());
        task.append(formatted);
    }

    private sealed interface Computed {
        record Text(String value) implements Computed {}
        record Number(String value, double original) implements Computed {}

        String value();
    }

    private void addInlineExpression(Task task, InlineExpression expression) {
        StringBuilder builder = task.builder();
        Computed computed = computeExpression(task, expression);
        builder.append(computed.value());
    }

    private Computed computeExpression(Task task, InlineExpression expression) {
        return switch (expression) {
            case InlineExpression.StringLiteral(String value) -> new Computed.Text(value);
            case InlineExpression.NumberLiteral(double value) -> new Computed.Number(String.valueOf(value), value);
            case InlineExpression.VariableReference(String id) -> {
                Object placeholder = task.variables().get(id);
                if (placeholder instanceof Number number) {
                    yield new Computed.Number(number.toString(), number.doubleValue());
                }

                yield new Computed.Text(String.valueOf(placeholder));
            }

            case InlineExpression.FunctionalReference(String id, List<Argument> arguments) ->
                    new Computed.Text("FUNCTION_CALL(ID: %s | ARGS: %s)".formatted(id, arguments));

            case InlineExpression.MessageReference(String id, Optional<String> attribute) -> {
                Message.Interpolated refMsg = resource.message(id).interpolated(task.variables());
                String referenceContent = attribute
                        .map(termId -> refMsg.attributes().get(termId))
                        .orElse(refMsg.value());

                yield new Computed.Text(referenceContent);
            }

            case InlineExpression.TermReference(String id, Optional<String> attribute, FList<Argument> arguments) -> {
                Map<String, Object> resolvedArguments = arguments.stream()
                        .map(Argument.Named.class::cast)
                        .collect(Collectors.toMap(Argument.Named::name, named -> switch (named.expression()) {
                            case InlineExpression.StringLiteral(String value) -> value;
                            case InlineExpression.NumberLiteral(double value) -> value;
                        }));

                Message.Interpolated refTerm = resource.term(id).interpolated(resolvedArguments);
                String referenceContent = attribute
                        .map(termId -> refTerm.attributes().get(termId))
                        .orElse(refTerm.value());

                yield new Computed.Text(referenceContent);
            }
        };
    }
}
