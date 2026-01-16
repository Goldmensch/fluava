package dev.goldmensch.fluava.test.parser.misc;

import io.github.parseworks.Result;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static dev.goldmensch.fluava.test.parser.Helper.parse;

/// uses Latin#lowerAlpha, Latin#upperAlpha, Latin#number
@DisplayName("MiscP#identifier")
public class IdentifierTest {

    private <T> Result<Character, T> parseIdentifier(String content) {
        return parse("ast.MiscP", "identifier", content);
    }

    @Test
    void simple_success() {
        String content = "abc";
        Result<Character, String> result = parseIdentifier(content);

        Assertions.assertTrue(result.isSuccess());
        Assertions.assertEquals(content, result.get());
    }

    @Test
    void number_success() {
        String content = "abc123";
        Result<Character, String> result = parseIdentifier(content);

        Assertions.assertTrue(result.isSuccess());
        Assertions.assertEquals(content, result.get());
    }

    @Test
    void underscore_success() {
        String content = "abc_123";
        Result<Character, String> result = parseIdentifier(content);

        Assertions.assertTrue(result.isSuccess());
        Assertions.assertEquals(content, result.get());
    }

    @Test
    void hyphen_success() {
        String content = "abc-123";
        Result<Character, String> result = parseIdentifier(content);

        Assertions.assertTrue(result.isSuccess());
        Assertions.assertEquals(content, result.get());
    }

    @Test
    void underscore_at_begin_error() {
        String content = "_abc";
        Result<Character, String> result = parseIdentifier(content);

        Assertions.assertTrue(result.isError());
    }

    @Test
    void hyphen_at_begin_error() {
        String content = "-abc";
        Result<Character, String> result = parseIdentifier(content);

        Assertions.assertTrue(result.isError());
    }

    @Test
    void number_at_begin_error() {
        String content = "123abc";
        Result<Character, String> result = parseIdentifier(content);

        Assertions.assertTrue(result.isError());
    }

    @Test
    void non_latin_error() {
        String content = "Д";
        Result<Character, String> result = parseIdentifier(content);

        Assertions.assertTrue(result.isError());
    }

    @Test
    void empty() {
        String content = "";
        Result<Character, String> result = parseIdentifier(content);

        Assertions.assertTrue(result.isError());
    }
}
