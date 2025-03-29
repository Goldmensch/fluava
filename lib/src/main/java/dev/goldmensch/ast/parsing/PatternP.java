package dev.goldmensch.ast.parsing;

import dev.goldmensch.ast.parsing.tree.pattern.PatternElement;
import io.github.parseworks.FList;
import io.github.parseworks.Parser;
import io.github.parseworks.Ref;
import io.github.parseworks.Utils;

import static dev.goldmensch.ast.parsing.ExpressionP.inline_expression;
import static dev.goldmensch.ast.parsing.ExpressionP.select_expression;
import static dev.goldmensch.ast.parsing.MiscP.*;
import static io.github.parseworks.Combinators.chr;
import static io.github.parseworks.Combinators.oneOf;

@SuppressWarnings("unchecked")
class PatternP {


    // Text
    private static final Parser<Character, String> inline_text = text_char.many().map(Utils::listToString);

    private static final Parser<Character, Character> indented_char = text_char
            .not(oneOf(chr('['), chr('*'), chr('.')));

    private static final Parser<Character, String> block_text = blank_block
            .then(blank_inline)
            .then(indented_char)
            .then(inline_text.optional())
            .map(a -> b -> c -> d -> a + b + c + d.orElse(""));

    // Placeable
    static final Ref<Character, PatternElement> inline_placeable = Parser.ref();

    private static final Parser<Character, PatternElement> block_placeable = blank_block.optional()
            .skipThen(opt_blank_inline)
            .skipThen(inline_placeable);

    // Pattern
    private static final Parser<Character, PatternElement> pattern_element = oneOf(
            inline_text.map(PatternElement.Text::new),
            block_text.map(PatternElement.Text::new),
            inline_placeable,
            block_placeable
    );

    static final Parser<Character, FList<PatternElement>> pattern = pattern_element.many();

    static {
        inline_placeable.set(
                chr('{')
                .skipThen(blank.optional())
                .skipThen(oneOf(
                        (Parser<Character, PatternElement>) (Parser<Character, ?>) select_expression,
                        (Parser<Character, PatternElement>) (Parser<Character, ?>) inline_expression
                ))
                .thenSkip(blank.optional())
                .thenSkip(chr('}'))
        );
    }
}
