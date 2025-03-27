package dev.goldmensch.ast.parsing;

import io.github.parseworks.FList;
import io.github.parseworks.Parser;
import io.github.parseworks.Utils;

import static io.github.parseworks.Combinators.*;
import static io.github.parseworks.Combinators.chr;

public class Misc {

    static final Parser<Character, Character> special_text_char = chr('{').or(chr('}'));

    static final Parser<Character, Character> line_end = oneOf(
            chr('\r').skipThen(chr('\n').as('\n')),
            chr('\n')
    );
    static final Parser<Character, Character> text_char = any(Character.class)
            .not(oneOf(special_text_char, line_end));

    public static final Parser<Character, String> blank_inline = chr(' ').many().map(Utils::listToString);

    static final Parser<Character, String> blank_block = blank_inline.optional()
            .then(line_end)
            .map(a -> b -> a.orElse("") + b);

    static final Parser<Character, Character> indented_char = text_char
            .not(oneOf(chr('['), chr(']'), chr('.')));

    static final Parser<Character, FList<String>> blank = blank_inline.or(line_end.map(Object::toString)).many();
}
