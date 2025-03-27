package dev.goldmensch.ast.parsing;

import dev.goldmensch.ast.parsing.tree.PatternElement;
import io.github.parseworks.FList;
import io.github.parseworks.Parser;
import io.github.parseworks.Ref;
import io.github.parseworks.Utils;

import static dev.goldmensch.ast.parsing.Expression.inline_expression;
import static dev.goldmensch.ast.parsing.Expression.select_expression;
import static dev.goldmensch.ast.parsing.Misc.*;
import static io.github.parseworks.Combinators.chr;
import static io.github.parseworks.Combinators.oneOf;

@SuppressWarnings("unchecked")
public class PatternP {

    static final Ref<Character, PatternElement.PlaceableExpression> inline_placeable = Parser.ref();

    static final Parser<Character, String> inline_text = text_char.many().map(Utils::listToString);

    static final Parser<Character, String> block_text = blank_block
            .then(blank_inline)
            .then(indented_char)
            .then(inline_text.optional())
            .map(a -> b -> c -> d -> a + b + c + d.orElse(""));

    static final Parser<Character, PatternElement.PlaceableExpression> block_placeable = blank_block
            .skipThen(blank_block.optional())
            .skipThen(inline_placeable);

    static final Parser<Character, PatternElement> pattern_element = oneOf(
            inline_text.map(PatternElement.Text::new),
            block_text.map(PatternElement.Text::new),
            inline_placeable.map(PatternElement.Placeable::new),
            block_placeable.map(PatternElement.Placeable::new)
    );

    static final Parser<Character, FList<PatternElement>> pattern = pattern_element.many();

    static {
        inline_placeable.set(chr('{')
                .skipThen(blank.optional())
                .skipThen(oneOf(
                        (Parser<Character, PatternElement.PlaceableExpression>) (Parser<Character, ?>) inline_expression,
                        (Parser<Character, PatternElement.PlaceableExpression>) (Parser<Character, ?>) select_expression
                ))
                .thenSkip(blank.optional())
                .thenSkip(chr('}'))
        );
    }
}
