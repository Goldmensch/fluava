package dev.goldmensch.ast.parsing;

import dev.goldmensch.ast.parsing.tree.*;
import io.github.parseworks.*;
import io.github.parseworks.impl.Failure;

import java.util.function.Function;

import static dev.goldmensch.ast.parsing.EntryP.entry;
import static dev.goldmensch.ast.parsing.MiscP.*;
import static io.github.parseworks.Combinators.*;

public final class FluentParser implements Function<String, Resource> {

    public static void main(String[] args) {
        String text = """
emails = You have { $unreadEmails } unread emails.
emails2 = You have { NUMBER($unreadEmails) } unread emails.

last-notice =
    Last checked: { DATETIME($lastChecked, day: "numeric", month: "long") }.

                """;

        Resource res = new FluentParser().apply(text);
        for (Resource.ResourceComponent entry : res.components()) {
            System.out.println();
            System.out.println(entry);
        }
    }

    // Junk
    private static final Parser<Character, String> junk_line = any(Character.class)
            .not(chr('\n'))
            .zeroOrMany()
            .map(Utils::listToString)
            .then(chr('\n').or(Combinators.<Character>eof().as('\u0000')))
            .map(s -> c -> s + c);

    private static final Parser<Character, Resource.ResourceComponent> junk = junk_line
            .then(junk_line
                    .not(oneOf(string("#"), string("#"), chr(ch -> Latin.lowerAlpha(ch) || Latin.upperAlpha(ch)).map(Object::toString)))
                    .zeroOrMany()
            )
            .map(a -> b -> new Resource.ResourceComponent.Junk(a + String.join("", b)));

    // Resource
    private static final Parser<Character, Resource> resource = oneOf(
            entry.map(Resource.ResourceComponent.class::cast),
            blank_block.as(new Resource.ResourceComponent.Blank())
            ,junk
    ).zeroOrMany()
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
