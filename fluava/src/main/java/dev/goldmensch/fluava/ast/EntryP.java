package dev.goldmensch.fluava.ast;

import dev.goldmensch.fluava.ast.tree.entry.Comment;
import dev.goldmensch.fluava.ast.tree.entry.Entry;
import dev.goldmensch.fluava.ast.tree.entry.Term;
import dev.goldmensch.fluava.ast.tree.message.Attribute;
import dev.goldmensch.fluava.ast.tree.message.AstMessage;
import dev.goldmensch.fluava.ast.tree.pattern.Pattern;
import io.github.parseworks.Pair;
import io.github.parseworks.Parser;
import io.github.parseworks.Utils;

import java.util.Optional;

import static dev.goldmensch.fluava.ast.MiscP.*;
import static dev.goldmensch.fluava.ast.PatternP.pattern;
import static io.github.parseworks.Combinators.*;

public class EntryP {

    // AstMessage
    private static final Parser<Character, Attribute> attribute = line_end
            .skipThen(blank.optional())
            .skipThen(chr('.'))
            .skipThen(identifier)
            .thenSkip(opt_blank_inline)
            .thenSkip(chr('='))
            .thenSkip(opt_blank_inline)
            .then(pattern.map(Pattern::new))
            .map(Attribute::new);

    private static final Parser<Character, Entry> message = identifier
            .thenSkip(opt_blank_inline)
            .thenSkip(chr('='))
            .thenSkip(opt_blank_inline)
            .then(oneOf(
                    pattern.map(Pattern::new).then(attribute.zeroOrMany()).map(Pair::new),
                    Parser.<Character, Pattern>pure(null).then(attribute.many()).map(Pair::new)
            ))
            .thenSkip(line_end)
            .map(identifier -> content -> new AstMessage(identifier, Optional.ofNullable(content.left()), content.right(), Optional.empty()));

    // Term
    private static final Parser<Character, Entry> term = chr('-')
            .skipThen(identifier)
            .thenSkip(opt_blank_inline)
            .thenSkip(chr('='))
            .thenSkip(opt_blank_inline)
            .then(pattern.map(Pattern::new))
            .then(attribute.zeroOrMany())
            .map(identifier -> pattern -> attributes -> new Term(identifier, pattern, attributes, Optional.empty()));

    // Comment
    private static final Parser<Character, Character> comment_char = any(Character.class).not(line_end);

    private static final Parser<Character, Entry> comment_line = oneOf(
            string("###").as(Comment.Type.TRIPLE),
            string("##").as(Comment.Type.DOUBLE),
            string("#").as(Comment.Type.SINGLE)
    )
            .then(chr(' ').skipThen(comment_char.zeroOrMany())
                    .map(Utils::listToString)
                    .optional()
            )
            .thenSkip(line_end)
            .map(type -> content -> new Comment(type, content.orElse("")));

    // Entry
    static final Parser<Character, Entry> entry = oneOf(
            message,
            term,
            comment_line
    );
}
