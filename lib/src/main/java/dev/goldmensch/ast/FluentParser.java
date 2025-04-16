package dev.goldmensch.ast;

import dev.goldmensch.ast.tree.Resource;
import dev.goldmensch.ast.tree.entry.Comment;
import io.github.parseworks.*;
import io.github.parseworks.impl.Failure;

import java.util.function.Function;

import static dev.goldmensch.ast.EntryP.entry;
import static dev.goldmensch.ast.MiscP.blank_block;
import static io.github.parseworks.Combinators.*;

public final class FluentParser implements Function<String, Resource> {

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
            .map(FluentParser::joinComments)
            .map(Resource::new);

    private static FList<Resource.ResourceComponent> joinComments(FList<Resource.ResourceComponent> components) {
        FList<Resource.ResourceComponent> result = new FList<>();

        for (Resource.ResourceComponent current : components) {
            if (current instanceof Comment(
                    Comment.Type cType, String cContent
            ) && !result.isEmpty() && result.getLast() instanceof Comment(Comment.Type lType, String lContent) &&
                cType == lType) {
                result.removeLast();
                String content = lContent + "\n" + cContent;
                result.add(new Comment(cType, content));
            } else {
                result.add(current);
            }

        }
        return result;
    }

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
