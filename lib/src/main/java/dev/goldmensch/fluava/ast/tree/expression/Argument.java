package dev.goldmensch.fluava.ast.tree.expression;

public sealed interface Argument permits Argument.Named, InlineExpression {
    sealed interface NamedExpression permits InlineExpression.NumberLiteral, InlineExpression.StringLiteral {}
    record Named(String name, NamedExpression expression) implements Argument {}
}
