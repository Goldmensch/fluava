package dev.goldmensch.fluava.ast;

import dev.goldmensch.fluava.Result;
import dev.goldmensch.fluava.ast.tree.AstResource;
import dev.goldmensch.fluava.ast.tree.entry.Comment;
import dev.goldmensch.fluava.ast.tree.entry.Term;
import dev.goldmensch.fluava.ast.tree.message.AstMessage;
import io.github.parseworks.Combinators;
import io.github.parseworks.FList;
import io.github.parseworks.Parser;
import io.github.parseworks.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.function.Function;

import static dev.goldmensch.fluava.ast.EntryP.entry;
import static dev.goldmensch.fluava.ast.MiscP.blank_block;
import static io.github.parseworks.Combinators.*;

public final class FluentParser implements Function<String, Result<AstResource>> {

    private static final Logger log = LoggerFactory.getLogger(FluentParser.class);

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
            blank_block.as(new AstResource.ResourceComponent.Blank()),
            junk
    ).zeroOrMany()
            .map(FluentParser::joinComments)
            .map(FluentParser::pairCommentsWithTermsAndMessages)
            .map(AstResource::new);

    private static FList<AstResource.ResourceComponent> pairCommentsWithTermsAndMessages(FList<AstResource.ResourceComponent> components) {
        FList<AstResource.ResourceComponent> result = new FList<>();

        for (AstResource.ResourceComponent component : components) {

            String lastComment = null;
            if (!result.isEmpty() && result.getLast() instanceof Comment(Comment.Type type, String content) && type == Comment.Type.SINGLE) {
                lastComment = content;
            }

            final String finalLastComment = lastComment;
            var transformed = switch (component) {
                case AstMessage message when finalLastComment != null -> {
                    result.removeLast();
                    yield new AstMessage(message.id(), message.content(), message.attributes(), Optional.of(lastComment));
                }
                case Term term when  finalLastComment != null -> {
                    result.removeLast();
                    yield new Term(term.id(), term.pattern(), term.attributes(), Optional.of(lastComment));
                }
                default -> component;
            };
            result.add(transformed);
        }

        return result;
    };

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
            AstResource resource = result.get();
            for (AstResource.ResourceComponent component : resource.components()) {
                if (!(component instanceof AstResource.ResourceComponent.Junk(String content))) continue;

                log.warn("Couldn't parse fluent file content {}", content);
            }

            return new Result.Success<>(result.get());
        }
        return new Result.Failure<>(result.fullErrorMessage());
    }
}
