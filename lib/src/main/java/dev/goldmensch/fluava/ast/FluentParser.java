package dev.goldmensch.fluava.ast;

import dev.goldmensch.fluava.Result;
import dev.goldmensch.fluava.ast.tree.AstResource;
import dev.goldmensch.fluava.ast.tree.entry.Comment;
import io.github.parseworks.Combinators;
import io.github.parseworks.FList;
import io.github.parseworks.Parser;
import io.github.parseworks.Utils;

import java.util.function.Function;

import static dev.goldmensch.fluava.ast.EntryP.entry;
import static dev.goldmensch.fluava.ast.MiscP.blank_block;
import static io.github.parseworks.Combinators.*;

public final class FluentParser implements Function<String, Result<AstResource>> {

    // Junk
    private static final Parser<Character, String> junk_line = any(Character.class)
            .not(chr('\n'))
            .zeroOrMany()
            .map(Utils::listToString)
            .then(chr('\n').or(Combinators.<Character>eof().as('\u0000')))
            .map(s -> c -> s + c);

    private static final Parser<Character, AstResource.ResourceComponent> junk = junk_line
            .then(junk_line
                    .not(oneOf(string("#"), string("#"), chr(ch -> Latin.lowerAlpha(ch) || Latin.upperAlpha(ch)).map(Object::toString)))
                    .zeroOrMany()
            )
            .map(a -> b -> new AstResource.ResourceComponent.Junk(a + String.join("", b)));

    // AstResource
    private static final Parser<Character, AstResource> resource = oneOf(
            entry.map(AstResource.ResourceComponent.class::cast),
            blank_block.as(new AstResource.ResourceComponent.Blank())
            ,junk
    ).zeroOrMany()
            .map(FluentParser::joinComments)
            .map(AstResource::new);

    private static FList<AstResource.ResourceComponent> joinComments(FList<AstResource.ResourceComponent> components) {
        FList<AstResource.ResourceComponent> result = new FList<>();

        for (AstResource.ResourceComponent current : components) {
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
    public Result<AstResource> apply(String s) {
        io.github.parseworks.Result<Character, AstResource> result = resource.parseAll(s);
        if (result.isSuccess()) {
            return new Result.Success<>(result.get());
        }
        return new Result.Failure<>(result.fullErrorMessage());
    }
}
