package dev.goldmensch.ast.parsing.tree;

import io.github.parseworks.FList;

import java.util.List;
import java.util.Optional;

public sealed interface InlineExpression extends Argument, PatternElement.PlaceableExpression permits InlineExpression.FunctionalReference, InlineExpression.MessageReference, InlineExpression.NumberLiteral, InlineExpression.StringLiteral, InlineExpression.TermReference, InlineExpression.VariableReference, PatternElement.Placeable {
    record StringLiteral(String value) implements InlineExpression, Argument.NamedArgumentExpression, SelectExpression.VariantKey {}
    record NumberLiteral(double value) implements InlineExpression, Argument.NamedArgumentExpression, SelectExpression.VariantKey {}
    record FunctionalReference(String identifier, List<Argument> arguments) implements InlineExpression {}
    record MessageReference(String id, Optional<String> attribute) implements InlineExpression {}
    record TermReference(String id, Optional<String> attribute, Optional<FList<Argument>> arguments) implements InlineExpression {}
    record VariableReference(String id) implements InlineExpression {}
}
