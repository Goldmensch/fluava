package dev.goldmensch.ast.tree.pattern;

import dev.goldmensch.ast.tree.expression.InlineExpression;
import dev.goldmensch.ast.tree.expression.SelectExpression;

public sealed interface PatternElement permits PatternElement.Placeable, PatternElement.Text {

    record Text(String message) implements PatternElement {}

    sealed interface Placeable extends PatternElement permits InlineExpression, SelectExpression {}
}
