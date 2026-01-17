package dev.goldmensch.fluava.test.parser.expression;

import dev.goldmensch.fluava.ast.tree.expression.InlineExpression;
import dev.goldmensch.fluava.test.parser.misc.IdentifierTest;
import io.github.parseworks.FList;
import io.github.parseworks.Ref;
import io.github.parseworks.Result;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static dev.goldmensch.fluava.test.parser.Helper.get;

@DisplayName("ExpressionP#inline_expression")
public class InlineExpressionTest {

    @SuppressWarnings("unchecked")
    private Result<Character, InlineExpression> parseInline(String content) {
        return get("ast.ExpressionP", "inline_expression", Ref.class).parseAll(content);
    }

    /// uses ExpressionP#string_literal - tested in [StringLiteralTest]
    @Test
    void string_literal__success() {
        String content = "\"abc\"";
        Result<Character, InlineExpression> result = parseInline(content);

        Assertions.assertTrue(result.isSuccess());
        Assertions.assertEquals(new InlineExpression.StringLiteral("abc"), result.get());
    }

    /// uses ExpressionP#number_literal - tested in [NumberLiteralTest]
    @Test
    void number_literal__success() {
        String content = "123456";
        Result<Character, InlineExpression> result = parseInline(content);

        Assertions.assertTrue(result.isSuccess());
        Assertions.assertEquals(new InlineExpression.NumberLiteral(123456), result.get());
    }

    /// uses ExpressionP#functional_reference - call arguments tested in [CallArgumentsTest]
    @Test
    void functional_reference__success() {
        String content = "abc()";
        Result<Character, InlineExpression> result = parseInline(content);

        Assertions.assertTrue(result.isSuccess());
        Assertions.assertEquals(new InlineExpression.FunctionalReference("abc", List.of()), result.get());
    }

    /// uses ExpressionP#message_reference, MiscP#identifier (tested in [IdentifierTest])
    @Test
    void message_reference__success() {
        String content = "my-message";
        Result<Character, InlineExpression> result = parseInline(content);

        Assertions.assertTrue(result.isSuccess());
        Assertions.assertEquals(new InlineExpression.MessageReference("my-message", Optional.empty()), result.get());
    }

    /// uses ExpressionP#message_reference, MiscP#identifier (tested in [IdentifierTest])
    @Test
    void message_reference_with_attribute__success() {
        String content = "my-message.attr1";
        Result<Character, InlineExpression> result = parseInline(content);

        Assertions.assertTrue(result.isSuccess());
        Assertions.assertEquals(new InlineExpression.MessageReference("my-message", Optional.of("attr1")), result.get());
    }

    /// uses ExpressionP#term_reference, MiscP#identifier (tested in [IdentifierTest])
    @Test
    void term_reference__success() {
        String content = "-my-term";
        Result<Character, InlineExpression> result = parseInline(content);

        Assertions.assertTrue(result.isSuccess());
        Assertions.assertEquals(new InlineExpression.TermReference("my-term", Optional.empty(), FList.of()), result.get());
    }

    /// uses ExpressionP#term_reference, MiscP#identifier (tested in [IdentifierTest])
    @Test
    void term_reference_with_attribute__success() {
        String content = "-my-term.attr1";
        Result<Character, InlineExpression> result = parseInline(content);

        Assertions.assertTrue(result.isSuccess());
        Assertions.assertEquals(new InlineExpression.TermReference("my-term", Optional.of("attr1"), FList.of()), result.get());
    }

    /// uses ExpressionP#term_reference, MiscP#identifier (tested in [IdentifierTest]), ExpressionP#call_arguments (tested in [CallArgumentsTest])
    @Test
    void term_reference_with_arguments__success() {
        String content = "-my-term()";
        Result<Character, InlineExpression> result = parseInline(content);

        Assertions.assertTrue(result.isSuccess());
        Assertions.assertEquals(new InlineExpression.TermReference("my-term", Optional.empty(), FList.of()), result.get());
    }

    /// uses ExpressionP#term_reference, MiscP#identifier (tested in [IdentifierTest]), ExpressionP#call_arguments (tested in [CallArgumentsTest])
    @Test
    void term_reference_with_attribute_and_arguments__success() {
        String content = "-my-term.attr1()";
        Result<Character, InlineExpression> result = parseInline(content);

        Assertions.assertTrue(result.isSuccess());
        Assertions.assertEquals(new InlineExpression.TermReference("my-term", Optional.of("attr1"), FList.of()), result.get());
    }

    /// uses ExpressionP#term_reference, MiscP#identifier (tested in [IdentifierTest])
    @Test
    void variable_reference__success() {
        String content = "$var";
        Result<Character, InlineExpression> result = parseInline(content);

        Assertions.assertTrue(result.isSuccess());
        Assertions.assertEquals(new InlineExpression.VariableReference("var"), result.get());
    }
}
