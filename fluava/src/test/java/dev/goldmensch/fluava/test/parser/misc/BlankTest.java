package dev.goldmensch.fluava.test.parser.misc;

import io.github.parseworks.FList;
import io.github.parseworks.Result;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static dev.goldmensch.fluava.test.parser.Helper.parse;

@DisplayName("MiscP#blank, MiscP#blank_inline")
public class BlankTest {

    private <T> Result<Character, T> parseBlank(String field, String content) {
        return parse("ast.MiscP", field, content);
    }

    @Nested
    @DisplayName("MiscP#blank_inline")
    class BlankInline {

        @Test
        void one__success() {
            String content = " ";
            Result<Character, String> result = parseBlank("blank_inline", content);

            Assertions.assertTrue(result.isSuccess());
            Assertions.assertEquals(content, result.get());
        }

        @Test
        void many__success() {
            String content = "      ";
            Result<Character, String> result = parseBlank("blank_inline", content);

            Assertions.assertTrue(result.isSuccess());
            Assertions.assertEquals(content, result.get());
        }

        @Test
        void non_whitespace__fail() {
            String content = "  non whitespace ";
            Result<Character, String> result = parseBlank("blank_inline", content);

            Assertions.assertTrue(result.isError());
        }

    }


    /// uses MiscP#blank
    @Nested
    @DisplayName("MiscP#blank")
    class Blank {
        @Test
        void one_line__success() {
            String content = "      ";
            Result<Character, FList<String>> result = parseBlank("blank", content);

            Assertions.assertTrue(result.isSuccess());
            Assertions.assertEquals(FList.of(content), result.get());
        }

        @Test
        void multiple_lines__success() {
            String content = "      \n    \n    ";
            Result<Character, FList<String>> result = parseBlank("blank", content);

            Assertions.assertTrue(result.isSuccess());
            Assertions.assertEquals(FList.of("      ", "\n","    ", "\n", "    "), result.get());
        }
    }
}
