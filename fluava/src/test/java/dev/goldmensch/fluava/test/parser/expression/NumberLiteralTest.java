package dev.goldmensch.fluava.test.parser.expression;

import io.github.parseworks.Result;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static dev.goldmensch.fluava.test.parser.Helper.parse;

/// uses Latin#number
@DisplayName("ExpressionP#number_literal")
public class NumberLiteralTest {

    private Result<Character, Double> parseNumberLiteral(String content) {
        return parse("ast.ExpressionP", "number_literal", content);
    }

    @Test
    void positive__success() {
        String content = "1234567890";
        Result<Character, Double> result = parseNumberLiteral(content);
        Assertions.assertTrue(result.isSuccess());
        Assertions.assertEquals(1234567890D, result.get());
    }

    @Test
    void negative__success() {
        String content = "-1234567890";
        Result<Character, Double> result = parseNumberLiteral(content);
        Assertions.assertTrue(result.isSuccess());
        Assertions.assertEquals(-1234567890D, result.get());
    }

    @Test
    void positive_floating_point__success() {
        String content = "1234567890.0987654321";
        Result<Character, Double> result = parseNumberLiteral(content);
        Assertions.assertTrue(result.isSuccess());
        Assertions.assertEquals(1234567890.0987654321D, result.get());
    }

    @Test
    void negative_floating_point__success() {
        String content = "-1234567890.0987654321";
        Result<Character, Double> result = parseNumberLiteral(content);
        Assertions.assertTrue(result.isSuccess());
        Assertions.assertEquals(-1234567890.0987654321D, result.get());
    }

    @Test
    void non_digit__error() {
        String content = "123doesn'tbelonghere456";
        Result<Character, Double> result = parseNumberLiteral(content);
        Assertions.assertTrue(result.isError());
    }

    @Test
    void whitespace_error() {
        String content = "123 456";
        Result<Character, Double> result = parseNumberLiteral(content);
        Assertions.assertTrue(result.isError());
    }

    @Test
    void empty_error() {
        String content = "";
        Result<Character, Double> result = parseNumberLiteral(content);
        Assertions.assertTrue(result.isError());
    }
}
