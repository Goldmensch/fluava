package dev.goldmensch.fluava.test.parser.expression;

import dev.goldmensch.fluava.ast.tree.expression.InlineExpression;
import dev.goldmensch.fluava.ast.tree.expression.SelectExpression;
import dev.goldmensch.fluava.ast.tree.expression.Variant;
import dev.goldmensch.fluava.ast.tree.pattern.Pattern;
import dev.goldmensch.fluava.ast.tree.pattern.PatternElement;
import dev.goldmensch.fluava.test.parser.misc.IdentifierTest;
import io.github.parseworks.FList;
import io.github.parseworks.Result;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static dev.goldmensch.fluava.test.parser.Helper.parse;

@DisplayName("ExpressionP#select_expression")
public class SelectExpressionTest {

    private Result<Character, Variant.VariantKey> parseVariantKey(String content) {
        return parse("ast.ExpressionP", "variant_key", content);
    }

    private Result<Character, SelectExpression> paresSelect(String content) {
        return parse("ast.ExpressionP", "select_expression", content);
    }

    /// uses MiscP#identifier (tested in [IdentifierTest])
    @Test
    void variant_key_identifier__success() {
        String content = "[abc]";
        Result<Character, Variant.VariantKey> result = parseVariantKey(content);

        Assertions.assertTrue(result.isSuccess());
        Assertions.assertEquals(new InlineExpression.StringLiteral("abc"), result.get());
    }

    /// uses MiscP#identifier (tested in [IdentifierTest])
    @Test
    void variant_key_number__success() {
        String content = "[123]";
        Result<Character, Variant.VariantKey> result = parseVariantKey(content);

        Assertions.assertTrue(result.isSuccess());
        Assertions.assertEquals(new InlineExpression.NumberLiteral(123), result.get());
    }

    /// uses MiscP#identifier (tested in [IdentifierTest])
    @Test
    void variant_key_with_blank__success() {
        String content = "[  123   ]";
        Result<Character, Variant.VariantKey> result = parseVariantKey(content);

        Assertions.assertTrue(result.isSuccess());
        Assertions.assertEquals(new InlineExpression.NumberLiteral(123), result.get());
    }

    /// uses MiscP#identifier (tested in [IdentifierTest]), MiscP#line_end, PatternP#pattern
    @Test
    void only_default_variant__success() {
        String content = """
                $var -> 
                  *[warn] foo
                """;
        Result<Character, SelectExpression> result = paresSelect(content);

        Assertions.assertTrue(result.isSuccess());
        SelectExpression expected = new SelectExpression(
                new InlineExpression.VariableReference("var"),
                new Variant(new InlineExpression.StringLiteral("warn"), new Pattern(FList.of(new PatternElement.Text("foo")))),
                FList.of(
                        new Variant(new InlineExpression.StringLiteral("warn"), new Pattern(FList.of(new PatternElement.Text("foo"))))
                ));

        Assertions.assertEquals(expected, result.get());
    }

    /// uses MiscP#identifier (tested in [IdentifierTest]), MiscP#line_end, PatternP#pattern
    @Test
    void default_variant_at_end_plus_other__success() {
        String content = """
                $var -> 
                  [one] the first, the only
                  [kick] bar
                  *[warn] foo
                """;
        Result<Character, SelectExpression> result = paresSelect(content);

        Assertions.assertTrue(result.isSuccess());
        SelectExpression expected = new SelectExpression(
                new InlineExpression.VariableReference("var"),
                new Variant(new InlineExpression.StringLiteral("warn"), new Pattern(FList.of(new PatternElement.Text("foo")))),
                FList.of(
                        new Variant(new InlineExpression.StringLiteral("one"), new Pattern(FList.of(new PatternElement.Text("the first, the only")))),
                        new Variant(new InlineExpression.StringLiteral("kick"), new Pattern(FList.of(new PatternElement.Text("bar")))),
                        new Variant(new InlineExpression.StringLiteral("warn"), new Pattern(FList.of(new PatternElement.Text("foo"))))
                )
        );

        Assertions.assertEquals(expected, result.get());
    }

    /// uses MiscP#identifier (tested in [IdentifierTest]), MiscP#line_end, PatternP#pattern
    @Test
    void default_variant_at_first_plus_other__success() {
        String content = """
                $var -> 
                  *[warn] foo
                  [one] the first, the only
                  [kick] bar
                """;
        Result<Character, SelectExpression> result = paresSelect(content);

        Assertions.assertTrue(result.isSuccess());
        SelectExpression expected = new SelectExpression(
                new InlineExpression.VariableReference("var"),
                new Variant(new InlineExpression.StringLiteral("warn"), new Pattern(FList.of(new PatternElement.Text("foo")))),
                FList.of(
                        new Variant(new InlineExpression.StringLiteral("warn"), new Pattern(FList.of(new PatternElement.Text("foo")))),
                        new Variant(new InlineExpression.StringLiteral("one"), new Pattern(FList.of(new PatternElement.Text("the first, the only")))),
                        new Variant(new InlineExpression.StringLiteral("kick"), new Pattern(FList.of(new PatternElement.Text("bar")))
                        ))
        );

        Assertions.assertEquals(expected, result.get());
    }

    /// uses MiscP#identifier (tested in [IdentifierTest]), MiscP#line_end, PatternP#pattern
    @Test
    void default_variant_in_middle_plus_other__success() {
        String content = """
                $var -> 
                  [one] the first, the only
                  *[warn] foo
                  [kick] bar
                """;
        Result<Character, SelectExpression> result = paresSelect(content);

        Assertions.assertTrue(result.isSuccess());
        SelectExpression expected = new SelectExpression(
                new InlineExpression.VariableReference("var"),
                new Variant(new InlineExpression.StringLiteral("warn"), new Pattern(FList.of(new PatternElement.Text("foo")))),
                FList.of(
                        new Variant(new InlineExpression.StringLiteral("one"), new Pattern(FList.of(new PatternElement.Text("the first, the only")))),
                        new Variant(new InlineExpression.StringLiteral("warn"), new Pattern(FList.of(new PatternElement.Text("foo")))),
                        new Variant(new InlineExpression.StringLiteral("kick"), new Pattern(FList.of(new PatternElement.Text("bar")))
                        ))
        );

        Assertions.assertEquals(expected, result.get());
    }

    /// uses MiscP#identifier (tested in [IdentifierTest]), MiscP#line_end, PatternP#pattern
    @Test
    void missing_default_variant__error() {
        String content = """
                $var -> 
                  [one] the first, the only
                  [kick] bar
                """;
        Result<Character, SelectExpression> result = paresSelect(content);

        Assertions.assertTrue(result.isError());
    }

    /// uses MiscP#identifier (tested in [IdentifierTest]), MiscP#line_end, PatternP#pattern
    @Test
    void no_variant__error() {
        String content = """
                $var -> 
                """;
        Result<Character, SelectExpression> result = paresSelect(content);

        Assertions.assertTrue(result.isError());
    }
}
