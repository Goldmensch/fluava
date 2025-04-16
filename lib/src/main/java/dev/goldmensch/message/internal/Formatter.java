package dev.goldmensch.message.internal;

import dev.goldmensch.message.Message;
import dev.goldmensch.resource.Resource;
import dev.goldmensch.ast.tree.expression.Argument;
import dev.goldmensch.ast.tree.expression.InlineExpression;
import dev.goldmensch.ast.tree.expression.SelectExpression;
import dev.goldmensch.ast.tree.pattern.Pattern;
import dev.goldmensch.ast.tree.pattern.PatternElement;
import io.github.parseworks.FList;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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

    public String apply(Map<String, Object> variables) {
        if (components == null) return "";

        Task task = new Task(new StringBuilder(), variables);

        for (PatternElement component : components) {
            add(task, component);
        }

        return task.builder().toString();
    }

    private void add(Task task, PatternElement element) {
        switch (element) {
            case PatternElement.Text(String val) -> task.builder().append(val);
            case PatternElement.Placeable placeable -> addPlaceable(task, placeable);
        }
    }

    private void addPlaceable(Task task, PatternElement.Placeable placeable) {
        switch (placeable) {
            case InlineExpression inline -> addInline(task, inline);
            case SelectExpression select -> addSelect(task, select);
        }
    }

    private void addSelect(Task task, SelectExpression expression) {
        task.builder().append("SELECT()");

    }

    private void addInline(Task task, InlineExpression expression) {
        StringBuilder builder = task.builder();
        switch (expression) {
            case InlineExpression.StringLiteral(String value) -> builder.append(value);
            case InlineExpression.NumberLiteral(double value) -> builder.append(value);
            case InlineExpression.VariableReference(String id) -> {
                Object placeholder = task.variables().get(id);
                builder.append(placeholder);
            }

            case InlineExpression.FunctionalReference(String id, List<Argument> arguments) -> {
                builder.append("FUNCTION_CALL(ID: %s | ARGS: %s)".formatted(id, arguments));
            }
            case InlineExpression.MessageReference(String id, Optional<String> attribute) -> {
                Message.Interpolated refMsg = resource.message(id).interpolated(task.variables());
                String referenceContent = attribute
                        .map(termId -> refMsg.attributes().get(termId))
                        .orElse(refMsg.value());

                builder.append(referenceContent);
            }
            case InlineExpression.TermReference(String id, Optional<String> attribute, FList<Argument> arguments) -> {
                Map<String, Object> resolvedArguments = arguments.stream()
                        .map(Argument.Named.class::cast)
                        .collect(Collectors.toMap(Argument.Named::name, named -> switch (named.expression()) {
                          case InlineExpression.StringLiteral(String value) -> value;
                          case InlineExpression.NumberLiteral(double value)  -> value;
                        }));

                Message.Interpolated refTerm = resource.term(id).interpolated(resolvedArguments);
                String referenceContent = attribute
                        .map(termId -> refTerm.attributes().get(termId))
                        .orElse(refTerm.value());

                builder.append(referenceContent);
            }
        }
    }


}
