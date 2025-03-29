package dev.goldmensch.ast.parsing.tree.pattern;

import dev.goldmensch.ast.parsing.tree.expression.InlineExpression;
import dev.goldmensch.ast.parsing.tree.expression.SelectExpression;

public sealed interface PatternElement permits PatternElement.Placeable, PatternElement.Text {

    record Text(String message) implements PatternElement {}

    sealed interface Placeable extends PatternElement permits InlineExpression, SelectExpression {}
}
