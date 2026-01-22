package dev.goldmensch.fluava.test.formatter;

import dev.goldmensch.fluava.Fluava;
import dev.goldmensch.fluava.Resource;
import dev.goldmensch.fluava.ast.tree.expression.Argument;
import dev.goldmensch.fluava.ast.tree.expression.InlineExpression;
import dev.goldmensch.fluava.ast.tree.expression.SelectExpression;
import dev.goldmensch.fluava.ast.tree.expression.Variant;
import dev.goldmensch.fluava.ast.tree.pattern.Pattern;
import dev.goldmensch.fluava.ast.tree.pattern.PatternElement;
import dev.goldmensch.fluava.function.internal.FunctionConfigImpl;
import dev.goldmensch.fluava.function.internal.Functions;
import dev.goldmensch.fluava.internal.Formatter;
import io.github.parseworks.FList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static dev.goldmensch.fluava.test.formatter.Helper.format;

public class FormatterTest {

    @Test
    void string_literal() {
        FList<PatternElement> elements = FList.of(
                new InlineExpression.StringLiteral("val")
        );
        Pattern pattern = new Pattern(elements);
        String result = format(pattern);
        Assertions.assertEquals("val", result);
    }

    @Test
    void multiple_string_literal() {
        FList<PatternElement> elements = FList.of(
                new InlineExpression.StringLiteral("one"),
                new InlineExpression.StringLiteral(" "),
                new InlineExpression.StringLiteral("two"),
                new InlineExpression.StringLiteral(" "),
                new InlineExpression.StringLiteral("three")
        );
        Pattern pattern = new Pattern(elements);
        String result = format(pattern);
        Assertions.assertEquals("one two three", result);
    }

    @Test
    void number_literal() {
        FList<PatternElement> elements = FList.of(
                new InlineExpression.NumberLiteral(12.123)
        );
        Pattern pattern = new Pattern(elements);
        String result = format(pattern);
        Assertions.assertEquals("12.123", result);
    }

    // specified built-in function -> NUMBER
    @Test
    void function_reference_only_position() {
        FList<PatternElement> elements = FList.of(
                new InlineExpression.FunctionalReference("NUMBER", List.of(new InlineExpression.NumberLiteral(123)))
        );
        Pattern pattern = new Pattern(elements);
        String result = format(pattern);
        Assertions.assertEquals("123", result);
    }

    // specified built-in function -> NUMBER
    @Test
    void function_reference_with_named() {
        FList<PatternElement> elements = FList.of(
                new InlineExpression.FunctionalReference("NUMBER", List.of(
                        new InlineExpression.NumberLiteral(1.2),
                        new Argument.Named("style", new InlineExpression.StringLiteral("percent")))
                ));
        Pattern pattern = new Pattern(elements);
        String result = format(pattern);
        Assertions.assertEquals("120%", result);
    }

    @Test
    void message_reference() {
        FList<PatternElement> elements = FList.of(
                new InlineExpression.MessageReference("foo", Optional.of("attr1"))
        );
        Pattern pattern = new Pattern(elements);

        Functions functions = new Functions(new FunctionConfigImpl());
        Resource resource = Fluava.create(Locale.ENGLISH).of("""
                foo = Content
                    .attr1 = Bar
                """, Locale.ENGLISH).orElseThrow();
        Formatter formatter = new Formatter(functions, resource, pattern, "msg");

        String result = formatter.apply(Locale.ENGLISH, Map.of());
        Assertions.assertEquals("Bar", result);
    }

    @Test
    void term_reference() {
        FList<PatternElement> elements = FList.of(
                new InlineExpression.TermReference("foo", Optional.empty(), FList.of())
        );
        Pattern pattern = new Pattern(elements);

        Functions functions = new Functions(new FunctionConfigImpl());
        Resource resource = Fluava.create(Locale.ENGLISH).of("""
                -foo = Content
                    .attr1 = Bar
                """, Locale.ENGLISH).orElseThrow();
        Formatter formatter = new Formatter(functions, resource, pattern, "msg");

        String result = formatter.apply(Locale.ENGLISH, Map.of());
        Assertions.assertEquals("Content", result);
    }

    @Test
    void term_reference_with_arguments() {
        FList<PatternElement> elements = FList.of(
                new InlineExpression.TermReference("foo", Optional.empty(),
                        FList.of(new Argument.Named("var", new InlineExpression.StringLiteral("suck")))
                )
        );
        Pattern pattern = new Pattern(elements);

        Functions functions = new Functions(new FunctionConfigImpl());
        Resource resource = Fluava.create(Locale.ENGLISH).of("""
                -foo = Tests { $var }
                    .attr1 = Bar
                """, Locale.ENGLISH).orElseThrow();
        Formatter formatter = new Formatter(functions, resource, pattern, "msg");

        String result = formatter.apply(Locale.ENGLISH, Map.of());
        Assertions.assertEquals("Tests suck", result);
    }

    @Test
    void variable_reference() {
        FList<PatternElement> elements = FList.of(
                new InlineExpression.VariableReference("var")
        );
        Pattern pattern = new Pattern(elements);
        String result = format(pattern, Map.of("var", "foo"));
        Assertions.assertEquals("foo", result);
    }

    @Test
    void selector_found_literal_key() {
        Variant defaultVariant = new Variant(new InlineExpression.StringLiteral("other"), new Pattern(FList.of()));
        FList<Variant> variants = FList.of(
                defaultVariant,
                new Variant(new InlineExpression.StringLiteral("one"), new Pattern(FList.of(new InlineExpression.StringLiteral("foo")))),
                new Variant(new InlineExpression.StringLiteral("two"), new Pattern(FList.of()))
        );

        FList<PatternElement> elements = FList.of(
                new SelectExpression(new InlineExpression.StringLiteral("one"), defaultVariant, variants)
        );
        Pattern pattern = new Pattern(elements);
        String result = format(pattern);
        Assertions.assertEquals("foo", result);
    }

    @Test
    void selector_not_found_literal_key() {
        Variant defaultVariant = new Variant(new InlineExpression.StringLiteral("other"), new Pattern(FList.of(new InlineExpression.StringLiteral("fallback"))));
        FList<Variant> variants = FList.of(
                defaultVariant,
                new Variant(new InlineExpression.StringLiteral("one"), new Pattern(FList.of())),
                new Variant(new InlineExpression.StringLiteral("two"), new Pattern(FList.of()))
        );

        FList<PatternElement> elements = FList.of(
                new SelectExpression(new InlineExpression.StringLiteral("three"), defaultVariant, variants)
        );
        Pattern pattern = new Pattern(elements);
        String result = format(pattern);
        Assertions.assertEquals("fallback", result);
    }

    @Test
    void selector_cardinal_exact_number() {
        Variant defaultVariant = new Variant(new InlineExpression.StringLiteral("other"), new Pattern(FList.of()));
        FList<Variant> variants = FList.of(
                new Variant(new InlineExpression.StringLiteral("one"), new Pattern(FList.of())),
                new Variant(new InlineExpression.NumberLiteral(2), new Pattern(FList.of(new InlineExpression.StringLiteral("foo")))),
                defaultVariant
                );

        FList<PatternElement> elements = FList.of(
                new SelectExpression(new InlineExpression.NumberLiteral(2), defaultVariant, variants)
        );
        Pattern pattern = new Pattern(elements);
        String result = format(pattern);
        Assertions.assertEquals("foo", result);
    }

    @Test
    void selector_cardinal_plural_category() {
        Variant defaultVariant = new Variant(new InlineExpression.StringLiteral("other"), new Pattern(FList.of()));
        FList<Variant> variants = FList.of(
                new Variant(new InlineExpression.StringLiteral("one"), new Pattern(FList.of())),
                new Variant(new InlineExpression.StringLiteral("other"), new Pattern(FList.of(new InlineExpression.StringLiteral("foo")))),
                defaultVariant
        );

        FList<PatternElement> elements = FList.of(
                new SelectExpression(new InlineExpression.NumberLiteral(2), defaultVariant, variants)
        );
        Pattern pattern = new Pattern(elements);
        String result = format(pattern);
        Assertions.assertEquals("foo", result);
    }

    @Test
    void selector_ordinal_plural_rule() {
        Variant defaultVariant = new Variant(new InlineExpression.StringLiteral("other"), new Pattern(FList.of()));
        FList<Variant> variants = FList.of(
                new Variant(new InlineExpression.StringLiteral("one"), new Pattern(FList.of())),
                new Variant(new InlineExpression.StringLiteral("two"), new Pattern(FList.of(new InlineExpression.StringLiteral("foo")))),
                defaultVariant
        );

        FList<PatternElement> elements = FList.of(
                new SelectExpression(new InlineExpression.FunctionalReference("NUMBER",
                        List.of(
                                new InlineExpression.NumberLiteral(2),
                                new Argument.Named("type", new InlineExpression.StringLiteral("ordinal")))),
                        defaultVariant, variants)
        );
        Pattern pattern = new Pattern(elements);
        String result = format(pattern);
        Assertions.assertEquals("foo", result);
    }

    @Test
    void selector_ordinal_exact_number() {
        Variant defaultVariant = new Variant(new InlineExpression.StringLiteral("other"), new Pattern(FList.of()));
        FList<Variant> variants = FList.of(
                new Variant(new InlineExpression.NumberLiteral(2), new Pattern(FList.of(new InlineExpression.StringLiteral("foo")))),
                new Variant(new InlineExpression.StringLiteral("two"), new Pattern(FList.of())),
                defaultVariant
        );

        FList<PatternElement> elements = FList.of(
                new SelectExpression(new InlineExpression.FunctionalReference("NUMBER",
                        List.of(
                                new InlineExpression.NumberLiteral(2),
                                new Argument.Named("type", new InlineExpression.StringLiteral("ordinal")))),
                        defaultVariant, variants)
        );
        Pattern pattern = new Pattern(elements);
        String result = format(pattern);
        Assertions.assertEquals("foo", result);
    }


}
