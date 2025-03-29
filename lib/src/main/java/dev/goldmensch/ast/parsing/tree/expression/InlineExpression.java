package dev.goldmensch.ast.parsing.tree.expression;

import dev.goldmensch.ast.parsing.tree.pattern.PatternElement;
import io.github.parseworks.FList;

import java.util.List;
import java.util.Optional;

public sealed interface InlineExpression extends Argument, PatternElement.Placeable permits InlineExpression.FunctionalReference, InlineExpression.MessageReference, InlineExpression.NumberLiteral, InlineExpression.StringLiteral, InlineExpression.TermReference, InlineExpression.VariableReference {
    record StringLiteral(String value) implements InlineExpression, NamedExpression, Variant.VariantKey {}
    record NumberLiteral(double value) implements InlineExpression, NamedExpression, Variant.VariantKey {}
    record FunctionalReference(String id, List<Argument> arguments) implements InlineExpression {}
    record MessageReference(String id, Optional<String> attribute) implements InlineExpression {}
    record TermReference(String id, Optional<String> attribute, FList<Argument> arguments) implements InlineExpression {}
    record VariableReference(String id) implements InlineExpression {}
}
