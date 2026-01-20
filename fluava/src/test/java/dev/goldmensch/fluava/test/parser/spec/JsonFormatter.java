package dev.goldmensch.fluava.test.parser.spec;

import dev.goldmensch.fluava.ast.tree.AstResource;
import dev.goldmensch.fluava.ast.tree.entry.Comment;
import dev.goldmensch.fluava.ast.tree.entry.Entry;
import dev.goldmensch.fluava.ast.tree.entry.Term;
import dev.goldmensch.fluava.ast.tree.expression.Argument;
import dev.goldmensch.fluava.ast.tree.expression.InlineExpression;
import dev.goldmensch.fluava.ast.tree.expression.SelectExpression;
import dev.goldmensch.fluava.ast.tree.expression.Variant;
import dev.goldmensch.fluava.ast.tree.message.AstMessage;
import dev.goldmensch.fluava.ast.tree.message.Attribute;
import dev.goldmensch.fluava.ast.tree.pattern.Pattern;
import dev.goldmensch.fluava.ast.tree.pattern.PatternElement;
import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class JsonFormatter {

    public JSONObject toJson(AstResource resource) {
        List<JSONObject> entries = resource.components()
                .stream()
                .map(this::component)
                .filter(Objects::nonNull)
                .toList();


        return new JSONObject()
                .put("type", "Resource")
                .put("body", new JSONArray(entries));
    }

    private JSONObject component(AstResource.ResourceComponent component) {
        return switch (component) {
            case AstResource.ResourceComponent.Blank _ -> null;
            case AstResource.ResourceComponent.Junk(String content) -> new JSONObject()
                    .put("type", "Junk")
                    .put("annotations", new JSONArray())
                    .put("content", escape(content));
            case Entry entry -> entry(entry);
        };
    }

    private JSONObject entry(Entry entry) {
        return switch (entry) {
            case Comment comment -> comment(comment);
            case AstMessage message -> message(message);
            case Term term -> term(term);
        };
    }

    private JSONObject comment(Comment comment) {
        String type = switch (comment.type()) {
            case DOUBLE -> "GroupComment";
            case SINGLE -> "Comment";
            case TRIPLE -> "ResourceComment";
        };

        return new JSONObject()
                .put("type", type)
                .put("content", escape(comment.content()));
    }

    private JSONObject message(AstMessage message) {
        Object value = message.content()
                .map(this::pattern)
                .map(Object.class::cast)
                .orElse(JSONObject.NULL);

        return new JSONObject()
                .put("type", "Message")
                .put("id", identifier(message.id()))
                .put("value", value)
                .put("attributes", attributes(message.attributes()))
                .put("comment", commentInline(message.comment()));
    }

    private JSONObject term(Term term) {
        return new JSONObject()
                .put("type", "Term")
                .put("id", identifier(term.id()))
                .put("value", pattern(term.pattern()))
                .put("attributes", attributes(term.attributes()))
                .put("comment", commentInline(term.comment()));
    }

    private JSONArray attributes(List<Attribute> attributes) {
        List<JSONObject> elements = attributes.stream()
                .map(attribute -> new JSONObject()
                        .put("type", "Attribute")
                        .put("id", identifier(attribute.id()))
                        .put("value", pattern(attribute.pattern()))
                )
                .toList();
        return new JSONArray(elements);
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private Object commentInline(Optional<String> content) {
        return content
                .map(comment -> ((Object) new JSONObject()
                        .put("type", "Comment")
                        .put("content", comment)))
                .orElse(JSONObject.NULL);
    }

    private JSONObject pattern(Pattern pattern) {
        List<JSONObject> elements = pattern.components()
                .stream()
                .map(this::patternElement)
                .toList();

        return new JSONObject()
                .put("type", "Pattern")
                .put("elements", new JSONArray(elements));
    }

    private JSONObject patternElement(PatternElement element) {
        return switch (element) {
            case PatternElement.Text text -> text(text);
            case PatternElement.Placeable placeable -> placeable(placeable);
        };
    }

    private JSONObject text(PatternElement.Text text) {
        return new JSONObject()
                .put("type", "TextElement")
                .put("value", escape(text.message()));
    }

    private JSONObject placeable(PatternElement.Placeable placeable) {
        JSONObject expression = switch (placeable) {
            case InlineExpression inlineExpression -> inlineExpression(inlineExpression);
            case SelectExpression selectExpression -> selectExpression(selectExpression);
        };

        return new JSONObject()
                .put("type", "Placeable")
                .put("expression", expression);
    }

    private JSONObject selectExpression(SelectExpression selectExpression) {
        List<JSONObject> variants = selectExpression.others()
                .stream()
                .map((Variant variant) -> variant(variant, false))
                .collect(Collectors.toList());
        variants.add(variant(selectExpression.defaultVariant(), true));

        return new JSONObject()
                .put("type", "SelectExpression")
                .put("selector", inlineExpression(selectExpression.expression()))
                .put("variants", new JSONArray(variants));
    }

    private JSONObject variant(Variant variant, boolean isDefault) {
        return new JSONObject()
                .put("type", "Variant")
                .put("key", variantKey(variant.key()))
                .put("value", pattern(variant.pattern()))
                .put("default", isDefault);
    }

    private JSONObject variantKey(Variant.VariantKey variantKey) {
        return switch (variantKey) {
            case InlineExpression.StringLiteral(String name) -> identifier(name);
            case InlineExpression.NumberLiteral num -> inlineExpression(num);
        };
    }

    private JSONObject inlineExpression(InlineExpression inlineExpression) {
        return switch (inlineExpression) {
            case InlineExpression.StringLiteral(String content) -> new JSONObject()
                    .put("type", "StringLiteral")
                    .put("value", content);
            case InlineExpression.NumberLiteral(double number) -> new JSONObject()
                    .put("type", "NumberLiteral")
                    .put("value", BigDecimal.valueOf(number).stripTrailingZeros().toPlainString());
            case InlineExpression.VariableReference(String id) -> new JSONObject()
                    .put("type", "VariableReference")
                    .put("id", identifier(id));
            case InlineExpression.MessageReference ref -> messageRef(ref);
            case InlineExpression.TermReference ref -> termRef(ref);
            case InlineExpression.FunctionalReference ref -> functionRef(ref);
        };
    };

    private JSONObject messageRef(InlineExpression.MessageReference reference) {
        return new JSONObject()
                .put("type", "MessageReference")
                .put("id", identifier(reference.id()))
                .put("attribute", reference.attribute().<Object>map(this::identifier).orElse(JSONObject.NULL));
    }

    private JSONObject termRef(InlineExpression.TermReference reference) {
        return new JSONObject()
                .put("type", "TermReference")
                .put("id", identifier(reference.id()))
                .put("attribute", reference.attribute().<Object>map(this::identifier).orElse(JSONObject.NULL))
                .put("arguments", arguments(reference.arguments()));
    }

    private JSONObject functionRef(InlineExpression.FunctionalReference reference) {

        return new JSONObject()
                .put("type", "FunctionReference")
                .put("id", identifier(reference.id()))
                .put("arguments", arguments(reference.arguments()));
    }

    private JSONObject arguments(List<Argument> arguments) {
        List<JSONObject> positional = new ArrayList<>();
        List<JSONObject> named = new ArrayList<>();
        for (Argument argument : arguments) {
            switch (argument) {
                case Argument.Named(String name, Argument.NamedExpression expression) -> named.add(new JSONObject()
                        .put("type", "NamedArgument")
                        .put("name", identifier(name))
                        .put("value", inlineExpression((InlineExpression) expression)));

                case InlineExpression expr -> positional.add(inlineExpression(expr));
            }
        }

        return new JSONObject()
                .put("type", "CallArguments")
                .put("positional", positional)
                .put("named", named);
    }

    private JSONObject identifier(String name) {
        return new JSONObject()
                .put("type", "Identifier")
                .put("name", name);
    }

    private String escape(String raw) {
        return raw;
    }
}
