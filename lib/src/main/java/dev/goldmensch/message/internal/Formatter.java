package dev.goldmensch.message.internal;

import dev.goldmensch.ast.tree.expression.Argument;
import dev.goldmensch.ast.tree.expression.InlineExpression;
import dev.goldmensch.ast.tree.expression.SelectExpression;
import dev.goldmensch.ast.tree.expression.Variant;
import dev.goldmensch.ast.tree.pattern.Pattern;
import dev.goldmensch.ast.tree.pattern.PatternElement;
import dev.goldmensch.cldrplurals.PluralCategory;
import dev.goldmensch.cldrplurals.Plurals;
import dev.goldmensch.cldrplurals.Type;
import dev.goldmensch.function.Functions;
import dev.goldmensch.function.Value;
import dev.goldmensch.message.Message;
import dev.goldmensch.resource.Resource;
import io.github.parseworks.FList;

import java.util.*;
import java.util.stream.Collectors;

public class Formatter {
    public static final Formatter EMPTY = new Formatter();

    private final Functions functions;
    private final FList<PatternElement> components;
    private final Resource resource;

    private Formatter() {
        this.components = null;
        this.resource = null;
        this.functions = null;
    }

    public Formatter(Functions functions, Resource resource, Pattern ast) {
        this.functions = Objects.requireNonNull(functions);
        this.components = Objects.requireNonNull(ast).components();
        this.resource = Objects.requireNonNull(resource);
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
        Value<?> selector = computeExpression(task, expression.expression(), true);

        switch (selector) {
            case Value.Text(String computedValue) -> {
                for (Variant entry : expression.others()) {
                    if (entry.key() instanceof InlineExpression.StringLiteral(String value) && computedValue.equals(value)) {
                        addPattern(task, entry.pattern());
                        return;
                    }
                }
            }

            case Value.Number(String computedValue, Double original) -> {
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

            default -> throw new UnsupportedOperationException();
        }

        addPattern(task, expression.defaultVariant());
    }

    private void addPattern(Task task, Pattern pattern) {
        String formatted = new Formatter(functions, resource, pattern).apply(task.locale(), task.variables());
        task.append(formatted);
    }

    private void addInlineExpression(Task task, InlineExpression expression) {
        StringBuilder builder = task.builder();
        Value<?> computed = computeExpression(task, expression, true);
        builder.append(computed.stringValue());
    }

    private Value<?> computeExpression(Task task, InlineExpression expression, boolean implicitResolve) {
        return switch (expression) {
            case InlineExpression.StringLiteral(String value) -> new Value.Text(value);
            case InlineExpression.NumberLiteral(double value) -> functions.tryImplicit(task.locale(), value).orElseThrow();
            case InlineExpression.VariableReference(String id) -> {
                Object placeholder = task.variables().get(id);
                if (!implicitResolve) yield new Value.Raw(placeholder);

                yield functions.tryImplicit(task.locale(), placeholder)
                        .map(Value.class::cast)
                        .orElseGet(() -> new Value.Raw(placeholder));
            }

            case InlineExpression.FunctionalReference(String id, List<Argument> arguments) -> {
                Object positional = arguments.stream()
                        .filter(InlineExpression.class::isInstance)
                        .map(InlineExpression.class::cast)
                        .map(expr -> computeExpression(task, expr, false)) // don't resolve variables implicit, keep user defined object
                        .findAny()
                        .map(Value::value)
                        .orElseThrow();

                yield functions.call(task.locale(), id, positional, resolveArguments(arguments));
            }

            case InlineExpression.MessageReference(String id, Optional<String> attribute) -> {
                Message.Interpolated refMsg = resource.message(id).interpolated(task.variables());
                String referenceContent = attribute
                        .map(termId -> refMsg.attributes().get(termId))
                        .orElse(refMsg.value());

                yield new Value.Text(referenceContent);
            }

            case InlineExpression.TermReference(String id, Optional<String> attribute, FList<Argument> arguments) -> {
                Message.Interpolated refTerm = resource.term(id).interpolated(resolveArguments(arguments));
                String referenceContent = attribute
                        .map(termId -> refTerm.attributes().get(termId))
                        .orElse(refTerm.value());

                yield new Value.Text(referenceContent);
            }
        };
    }

    private Map<String, Object> resolveArguments(List<Argument> arguments) {
        return arguments.stream()
                .filter(Argument.Named.class::isInstance)
                .map(Argument.Named.class::cast)
                .collect(Collectors.toMap(Argument.Named::name, named -> switch (named.expression()) {
                    case InlineExpression.StringLiteral(String value) -> value;
                    case InlineExpression.NumberLiteral(double value) -> value;
                }));
    }
}
