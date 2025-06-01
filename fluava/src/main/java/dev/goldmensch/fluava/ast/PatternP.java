package dev.goldmensch.fluava.ast;

import dev.goldmensch.fluava.ast.tree.pattern.PatternElement;
import io.github.parseworks.*;

import java.util.Collection;
import java.util.List;

import static dev.goldmensch.fluava.ast.ExpressionP.inline_expression;
import static dev.goldmensch.fluava.ast.ExpressionP.select_expression;
import static dev.goldmensch.fluava.ast.MiscP.*;
import static io.github.parseworks.Combinators.*;

@SuppressWarnings("unchecked")
class PatternP {

    private record TypedPatternElement(PatternElement element, Type type) {
        private enum Type {
            BLOCK_BLANK,
            OTHER
        }

        private static TypedPatternElement block(PatternElement element) {
            return new TypedPatternElement(element, Type.BLOCK_BLANK);
        }
        private static TypedPatternElement other(PatternElement element) {
            return new TypedPatternElement(element, Type.OTHER);
        }
    }

    // Text
    private static final Parser<Character, TypedPatternElement> inline_text = text_char.many().map(Utils::listToString)
            .map(PatternElement.Text::new)
            .map(TypedPatternElement::other);

    private static final Parser<Character, Character> indented_char = text_char
            .not(oneOf(chr('['), chr('*'), chr('.')));

    private static final Parser<Character, FList<TypedPatternElement>> block_text = Combinators.sequence(List.of(
            blank_block.map(PatternElement.Text::new).map(TypedPatternElement::block),
            blank_inline.map(PatternElement.Text::new).map(TypedPatternElement::other),
            indented_char.map(c -> new PatternElement.Text(c + "")).map(TypedPatternElement::other),
            inline_text.optional().map(option -> option.orElse(new TypedPatternElement(new PatternElement.Text(""), TypedPatternElement.Type.OTHER)))
    )).map(FList::new);

    // Placeable
    static final Ref<Character, TypedPatternElement> inline_placeable = Parser.ref();

    private static final Parser<Character, FList<TypedPatternElement>> block_placeable = sequence(List.of(
            blank_block.map(PatternElement.Text::new).map(TypedPatternElement::block),
            opt_blank_inline.map(option -> option.orElse("")).map(PatternElement.Text::new).map(TypedPatternElement::other),
            inline_placeable
    )).map(FList::new);

    // Pattern
    private static final Parser<Character, FList<TypedPatternElement>> pattern_element = oneOf(
            inline_text.map(FList::of),
            block_text,
            inline_placeable.map(FList::of),
            block_placeable
    );

    static final Parser<Character, FList<PatternElement>> pattern = pattern_element
            .many()
            .map(fLists -> fLists.stream().flatMap(Collection::stream).toList())
            .map(FList::new)
            .map(PatternP::joinTextAndDeIndent);

    private static FList<PatternElement> joinTextAndDeIndent(FList<TypedPatternElement> elements) {
        // calculate intend
        int intend = Integer.MAX_VALUE;
        for (int i = 0; i < elements.size(); i++) {
            var current = elements.get(i);

            if (i != 0 && elements.get(i-1).type == TypedPatternElement.Type.BLOCK_BLANK && current.element instanceof PatternElement.Text(String content)) {
                intend = Math.min(intend, content.length() - content.stripLeading().length());
            }
        }


        FList<PatternElement> result = new FList<>();
        boolean isNewLine = false;
        for (TypedPatternElement current : elements) {
            // if first element is blank block, then don't add it to the result
            if (current == elements.getFirst() && current.type == TypedPatternElement.Type.BLOCK_BLANK) {
                isNewLine = true;
                result.add(new PatternElement.Text(""));
                continue;
            }

            if (!result.isEmpty() && current.element instanceof PatternElement.Text(String cContent)
                    && result.getLast() instanceof PatternElement.Text(String lContent)) {
                result.removeLast();
                String strippedContent = isNewLine && (intend != Integer.MAX_VALUE)
                        ? cContent.substring(intend)
                        : cContent;
                String content = lContent + strippedContent;
                if (content.isEmpty()) continue;
                result.add(new PatternElement.Text(content));
            } else {
                result.add(current.element);
            }

            isNewLine = current.type == TypedPatternElement.Type.BLOCK_BLANK;
        }

        return result;
    }

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
                        .map(TypedPatternElement::other)
        );
    }
}
