package dev.goldmensch.ast.parsing;

import dev.goldmensch.ast.parsing.tree.*;
import io.github.parseworks.*;

import java.util.List;
import java.util.Optional;

import static dev.goldmensch.ast.parsing.FluentParser.identifier;
import static dev.goldmensch.ast.parsing.Misc.*;
import static dev.goldmensch.ast.parsing.PatternP.inline_placeable;
import static dev.goldmensch.ast.parsing.PatternP.pattern;
import static io.github.parseworks.Combinators.*;

public class Expression {
    static final Parser<Character, Character> special_quoted_char = chr('\"')
            .or(chr('\\'));

    static final Parser<Character, Character> special_escape = chr('\\')
            .skipThen(special_quoted_char);

    static final Parser<Character, String> unicode_escape = oneOf(
            string("\\u").skipThen(chr(Latin::hexChar).repeat(4).map(characters -> Character.toString(Integer.valueOf(Utils.listToString(characters), 16)))),
            string("\\U").skipThen(chr(Latin::hexChar).repeat(6).map(characters -> Character.toString(Integer.valueOf(Utils.listToString(characters), 16))))
    );

    static final Parser<Character, String> quoted_char = oneOf(
            any(Character.class).not(oneOf(special_quoted_char, line_end)).map(Object::toString),
            special_escape.map(Object::toString),
            unicode_escape
    );

    static final Parser<Character, String> string_literal = chr('"')
            .skipThen(quoted_char.zeroOrMany())
            .thenSkip(chr('"'))
            .map(strings -> String.join("", strings));

    static final Parser<Character, FList<Character>> digits = chr(Latin::number).many();

    static final Parser<Character, Double> number_literal = chr('-').optional()
            .then(digits.map(Utils::listToString))
            .then((chr('.').skipThen(digits).map(a -> '.' + Utils.listToString(a))).optional())
            .map(a -> b -> c -> Double.parseDouble(a.orElse('+') + b + c.orElse("")));

    static final Parser<Character, Argument.NamedArgument> named_argument = identifier
            .thenSkip(blank.optional())
            .thenSkip(chr(':'))
            .thenSkip(blank.optional())
            .then(oneOf(string_literal.map(InlineExpression.StringLiteral::new), number_literal.map(InlineExpression.NumberLiteral::new)))
            .map(identifier -> expression -> new Argument.NamedArgument(identifier, expression));

    static final Ref<Character, InlineExpression> inline_expression = Parser.ref();

    @SuppressWarnings("unchecked")
    static final Parser<Character, Argument> argument = Combinators.oneOf(
            (Parser<Character, Argument>) (Parser<Character, ?>) named_argument,
            (Parser<Character, Argument>) (Parser<Character, ?>) inline_expression
    );

    static final Parser<Character, FList<Argument>> argument_list =
            (argument.thenSkip(blank.optional()).thenSkip(chr(',')).thenSkip(blank.optional()))
                    .zeroOrMany()
                    .then(argument.optional())
                    .map(arguments -> argument -> {
                        argument.ifPresent(arguments::add);
                        return arguments;
                    });

    static final Parser<Character, FList<Argument>> call_arguments = blank.optional()
            .skipThen(chr('('))
            .skipThen(blank.optional())
            .skipThen(argument_list)
            .thenSkip(blank.optional())
            .thenSkip(chr(')'));

    static final ApplyBuilder<Character, String, FList<Argument>> functional_reference = identifier
            .then(call_arguments);

    static final Parser<Character, String> attribute_accessor = chr('.')
            .skipThen(identifier);

    static final ApplyBuilder<Character, String, Optional<String>> message_reference = identifier
            .then(attribute_accessor.optional());

    static final ApplyBuilder<Character, String, Optional<String>>.ApplyBuilder3<Optional<FList<Argument>>> term_reference = chr('-')
            .skipThen(identifier)
            .then(attribute_accessor.optional())
            .then(call_arguments.optional());

    static final Parser<Character, String> variable_reference = chr('$')
            .skipThen(identifier);

    static final Parser<Character, SelectExpression.VariantKey> variant_key = chr('[')
            .skipThen(blank.optional())
            .skipThen(oneOf(
                    identifier.map(InlineExpression.StringLiteral::new),
                    number_literal.map(InlineExpression.NumberLiteral::new)
            ))
            .thenSkip(blank.optional())
            .thenSkip(chr(']'))
            .map(a -> a);

    static final Parser<Character, SelectExpression.Variant> variant = line_end
            .skipThen(blank.optional())
            .skipThen(variant_key)
            .thenSkip(blank_inline.optional())
            .then(pattern.map(Pattern::new))
            .map(key -> pattern -> new SelectExpression.Variant(key, pattern));

    static final Parser<Character, SelectExpression.Variant> default_variant = line_end
            .skipThen(blank.optional())
            .skipThen(chr('*'))
            .skipThen(variant_key)
            .thenSkip(blank_inline.optional())
            .then(pattern.map(Pattern::new))
            .map(key -> pattern -> new SelectExpression.Variant(key, pattern));

    private record Variants(SelectExpression.Variant defaultV, FList<SelectExpression.Variant> other) {}

    static final Parser<Character, Variants> variant_list = variant.zeroOrMany()
            .then(default_variant)
            .then(variant.zeroOrMany())
            .thenSkip(line_end)
            .map(first -> defaultVariants -> second -> {
                first.addAll(second);
                return new Variants(defaultVariants, first);
            });

    static final Parser<Character, SelectExpression> select_expression = inline_expression
            .thenSkip(blank.optional())
            .thenSkip(string("->"))
            .thenSkip(blank_inline.optional())
            .then(variant_list)
            .map(expr -> variants -> new SelectExpression(expr, variants.defaultV, variants.other));

    static {
        inline_expression.set(oneOf(List.of(
                string_literal.map(InlineExpression.StringLiteral::new),
                number_literal.map(InlineExpression.NumberLiteral::new),
                functional_reference.map(InlineExpression.FunctionalReference::new),
                message_reference.map(InlineExpression.MessageReference::new),
                term_reference.map(InlineExpression.TermReference::new),
                variable_reference.map(InlineExpression.VariableReference::new),
                inline_placeable.map(PatternElement.Placeable::new)
        )));
    }
}
