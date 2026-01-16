package dev.goldmensch.fluava.test.parser.expression;


import io.github.parseworks.Result;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static dev.goldmensch.fluava.test.parser.Helper.parse;

/// uses MiscP#line_end
@DisplayName("ExpressionP#string_literal")
public class StringLiteralTest {

    private Result<Character, String> parseStringLiteral(String content) {
        return parse("ast.ExpressionP", "string_literal", "\"%s\"".formatted(content));
    }

    @Test
    void plain__success() {
        String content = "Fluava is the best library 4 ever <3";
        Result<Character, String> result = parseStringLiteral(content);
        Assertions.assertTrue(result.isSuccess());
        Assertions.assertEquals(content, result.get());
    }

    @Test
    void unicode_short__success() {
        String content = "Unicode escaped character: \\u2014 yes";
        Result<Character, String> result = parseStringLiteral(content);

        Assertions.assertTrue(result.isSuccess());
        Assertions.assertEquals("Unicode escaped character: — yes", result.get());
    }

    @Test
    void special_escape_double_question_mark__success() {
        String content = "Unicode escaped character: \\\" yes";
        Result<Character, String> result = parseStringLiteral(content);

        Assertions.assertTrue(result.isSuccess());
        Assertions.assertEquals("Unicode escaped character: \" yes", result.get());
    }

    @Test
    void special_escape_backslash__success() {
        String content = "Unicode escaped character: \\\\ yes";
        Result<Character, String> result = parseStringLiteral(content);

        Assertions.assertTrue(result.isSuccess());
        Assertions.assertEquals("Unicode escaped character: \\ yes", result.get());
    }

    @Test
    void unicode_any__success() {
        String content = "Unicode escaped character: \\U01F602 yes";
        Result<Character, String> result = parseStringLiteral(content);

        Assertions.assertTrue(result.isSuccess());
        Assertions.assertEquals("Unicode escaped character: \uD83D\uDE02 yes", result.get());
    }

    @Test
    void line_end_unix__error() {
        String content = "Line end next \n next line";
        Result<Character, String> result = parseStringLiteral(content);

        Assertions.assertTrue(result.isError());
    }

    @Test
    void line_end_windows__error() {
        String content = "Line end next \r\n next line";
        Result<Character, String> result = parseStringLiteral(content);
        Assertions.assertTrue(result.isError());
    }

    @Test
    void double_question_mark__error() {
        String content = "Forbidden character: \" yes";
        Result<Character, String> result = parseStringLiteral(content);
        Assertions.assertTrue(result.isError());
    }

    @Test
    void backslash__error() {
        String content = "Forbidden character: \\ yes";
        Result<Character, String> result = parseStringLiteral(content);
        Assertions.assertTrue(result.isError());
    }

    @Test
    void empty_error() {
        String content = "";
        Result<Character, Double> result = parse("ast.ExpressionP", "string_literal", content); // don't add "
        Assertions.assertTrue(result.isError());
    }
}
