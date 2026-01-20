package dev.goldmensch.fluava.ast.tree.pattern;

import dev.goldmensch.fluava.ast.tree.expression.InlineExpression;
import dev.goldmensch.fluava.ast.tree.expression.SelectExpression;

public sealed interface PatternElement permits PatternElement.Placeable, PatternElement.Text, InlineExpression {

    record Text(String message) implements PatternElement {}

    sealed interface Placeable extends PatternElement permits InlineExpression, SelectExpression {}
}
