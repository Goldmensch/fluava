package dev.goldmensch.fluava.test.parser.expression;

import dev.goldmensch.fluava.ast.tree.expression.Argument;
import dev.goldmensch.fluava.ast.tree.expression.InlineExpression;
import io.github.parseworks.FList;
import io.github.parseworks.Result;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static dev.goldmensch.fluava.test.parser.Helper.parse;

/// uses ExpressionP#string_literal, ExpressionP#number_literal, ExpressionP#inline_expression
@DisplayName("ExpressionP#call_arguments")
public class CallArgumentsTest {

    private Result<Character, FList<Argument>> parseCallArguments(String content) {
        return parse("ast.ExpressionP", "call_arguments", content);
    }

    @Test
    void empty__success() {
        String content = "()";
        Result<Character, FList<Argument>> result = parseCallArguments(content);

        Assertions.assertTrue(result.isSuccess());
        Assertions.assertEquals(FList.of(), result.get());
    }

    /// uses ExpressionP#string_literal
    @Test
    void named_string_literal__success() {
        String content = "(name: \"foo\")";
        Result<Character, FList<Argument>> result = parseCallArguments(content);

        Assertions.assertTrue(result.isSuccess());
        Argument.Named expected = new Argument.Named("name", new InlineExpression.StringLiteral("foo"));
        Assertions.assertEquals(FList.of(expected), result.get());
    }

    /// uses ExpressionP#string_literal
    @Test
    void named_string_literal_many__success() {
        String content = "(name1: \"foo\", name2: \"foo\")";
        Result<Character, FList<Argument>> result = parseCallArguments(content);

        Assertions.assertTrue(result.isSuccess());
        Argument.Named expected1 = new Argument.Named("name1", new InlineExpression.StringLiteral("foo"));
        Argument.Named expected2 = new Argument.Named("name2", new InlineExpression.StringLiteral("foo"));
        Assertions.assertEquals(FList.of(expected1, expected2), result.get());
    }

    /// uses ExpressionP#number_literal
    @Test
    void named_number_literal__success() {
        String content = "(name: 123456)";
        Result<Character, FList<Argument>> result = parseCallArguments(content);

        Assertions.assertTrue(result.isSuccess());
        Argument.Named expected = new Argument.Named("name", new InlineExpression.NumberLiteral(123456));
        Assertions.assertEquals(FList.of(expected), result.get());
    }

    /// uses ExpressionP#number_literal
    @Test
    void named_number_literal_many__success() {
        String content = "(name1: 123456, name2: 123456)";
        Result<Character, FList<Argument>> result = parseCallArguments(content);

        Assertions.assertTrue(result.isSuccess());

        Argument.Named expected1 = new Argument.Named("name1", new InlineExpression.NumberLiteral(123456));
        Argument.Named expected2 = new Argument.Named("name2", new InlineExpression.NumberLiteral(123456));
        Assertions.assertEquals(FList.of(expected1, expected2), result.get());
    }

    /// uses ExpressionP#inline_expression
    /// variable reference could be any inline expression
    @Test
    void inline_expression_variable_reference__success() {
        String content = "($var)";
        Result<Character, FList<Argument>> result = parseCallArguments(content);

        Assertions.assertTrue(result.isSuccess());
        Assertions.assertEquals(FList.of(new InlineExpression.VariableReference("var")), result.get());
    }

    /// uses ExpressionP#inline_expression
    /// variable reference could be any inline expression
    @Test
    void inline_expression_and_named__success() {
        String content = "($var, name: 123)";
        Result<Character, FList<Argument>> result = parseCallArguments(content);

        Assertions.assertTrue(result.isSuccess());
        InlineExpression.VariableReference expected1 = new InlineExpression.VariableReference("var");
        Argument.Named expected2 = new Argument.Named("name", new InlineExpression.NumberLiteral(123));
        Assertions.assertEquals(FList.of(expected1, expected2), result.get());
    }
}
