package dev.goldmensch.ast.parsing;

import dev.goldmensch.ast.parsing.tree.*;
import io.github.parseworks.*;
import io.github.parseworks.impl.Failure;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import static dev.goldmensch.ast.parsing.Misc.*;
import static dev.goldmensch.ast.parsing.PatternP.pattern;
import static io.github.parseworks.Combinators.*;

public final class FluentParser implements Function<String, Resource> {

    public static void main(String[] args) {
        String text = "";

        Resource res = new FluentParser().apply(text);
        for (Entry entry : res.entries()) {
            System.out.println();
            System.out.println(entry);
        }
    }

    static final Parser<Character, String> identifier = chr(ch -> Latin.lowerAlpha(ch) || Latin.upperAlpha(ch))
            .then(chr(ch -> Latin.lowerAlpha(ch) || Latin.upperAlpha(ch) || Latin.number(ch) || ch == '_' || ch == '-').zeroOrMany())
            .map(start -> following -> start.toString() + Utils.listToString(following));

    static final Parser<Character, Attribute> attribute = line_end
            .skipThen(blank.optional())
            .skipThen(chr('.'))
            .skipThen(identifier)
            .thenSkip(blank_inline.optional())
            .thenSkip(chr('='))
            .thenSkip(blank_inline.optional())
            .then(pattern.map(Pattern::new))
            .map(id -> pattern -> new Attribute(id, pattern));

    private record MessageContent(Pattern pattern, FList<Attribute> attributes) {}

    private static final ApplyBuilder<Character, String, MessageContent> message = identifier
            .thenSkip(blank_inline.optional())
            .thenSkip(chr('='))
            .thenSkip(blank_inline.optional())
            .then(oneOf(
                    pattern.map(Pattern::new).then(attribute.zeroOrMany()).map(MessageContent::new),
                    attribute.many().map(attributes -> new MessageContent(null, attributes))
            ));

    private static final ApplyBuilder<Character, String, Pattern>.ApplyBuilder3<FList<Attribute>> term = chr('-')
            .skipThen(identifier)
            .thenSkip(blank_inline.optional())
            .thenSkip(chr('='))
            .thenSkip(blank_inline.optional())
            .then(pattern.map(Pattern::new))
            .then(attribute.zeroOrMany());

    private static final Parser<Character, Character> comment_char = any(Character.class).not(line_end);

    private static final Parser<Character, String> comment_line = chr('#').repeat(1, 3)
            .skipThen((chr(' ').skipThen(comment_char.zeroOrMany())).optional())
            .thenSkip(line_end)
            .map(characters -> characters.map(Utils::listToString).orElse(""));

    private static final Parser<Character, Entry> entry = oneOf(
            message.thenSkip(line_end).map(identifier -> content -> new Message(identifier, Optional.ofNullable(content.pattern), content.attributes)),
            term.thenSkip(line_end).map(Term::new),
            comment_line.map(CommentLine::new)
    );

    private static final Parser<Character, Character> junk_line = (any(Character.class).not(chr('\n'))).zeroOrMany()
            .thenSkip(chr('\r').optional())
            .as(' ');

    private static final ApplyBuilder<Character, Character, FList<Character>> junk = junk_line
            .then(junk_line.not(oneOf(chr('#'), chr('-'), chr(ch -> Latin.lowerAlpha(ch) || Latin.upperAlpha(ch)))).zeroOrMany());

    private static final Parser<Character, Resource> resource = oneOf(
            entry,
            blank_block.as(null),
            junk.map(a -> b -> null)
    )
            .zeroOrMany()
            .map(entries -> entries.filter(Objects::nonNull))
            .map(Resource::new);

    @Override
    public Resource apply(String s) {
        var result = resource.parseAll(s);
        System.out.println(result.fullErrorMessage());

        if (result instanceof Failure<Character, ?> fail) {
            Input<Character> next = fail.next();
            System.out.println(next);
        }

        return result.get();
    }
}
