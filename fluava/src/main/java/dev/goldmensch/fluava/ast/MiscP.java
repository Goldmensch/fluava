package dev.goldmensch.fluava.ast;

import io.github.parseworks.FList;
import io.github.parseworks.Parser;
import io.github.parseworks.Utils;

import java.util.Optional;

import static io.github.parseworks.Combinators.*;
import static io.github.parseworks.Combinators.chr;

class MiscP {

    static final Parser<Character, Character> special_text_char = chr('{').or(chr('}'));


    static final Parser<Character, Character> line_end = oneOf(
            string("\r\n").as('\n'),
            chr('\n')
    );

    static final Parser<Character, Character> text_char = any(Character.class)
            .not(oneOf(special_text_char, line_end));

    static final Parser<Character, String> blank_inline = chr(' ').many().map(Utils::listToString);
    static final Parser<Character, Optional<String>> opt_blank_inline = blank_inline.optional();

    static final Parser<Character, String> blank_block = opt_blank_inline
            .skipThen(line_end)
            .many()
            .map(lines -> "\n".repeat(lines.size()));

    static final Parser<Character, FList<String>> blank = blank_inline.or(line_end.map(Object::toString)).many();

    static final Parser<Character, String> identifier = chr(ch -> Latin.lowerAlpha(ch) || Latin.upperAlpha(ch))
            .then(chr(ch -> Latin.lowerAlpha(ch) || Latin.upperAlpha(ch) || Latin.number(ch) || ch == '_' || ch == '-').zeroOrMany())
            .map(start -> following -> start.toString() + Utils.listToString(following));
}
