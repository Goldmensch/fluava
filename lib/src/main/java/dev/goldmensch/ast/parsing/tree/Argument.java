package dev.goldmensch.ast.parsing.tree;

public sealed interface Argument permits Argument.NamedArgument, InlineExpression {
    sealed interface NamedArgumentExpression permits InlineExpression.NumberLiteral, InlineExpression.StringLiteral {}
    record NamedArgument(String name, NamedArgumentExpression expression) implements Argument {}
}
