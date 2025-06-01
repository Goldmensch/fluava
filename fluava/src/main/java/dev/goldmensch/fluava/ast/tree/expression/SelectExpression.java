package dev.goldmensch.fluava.ast.tree.expression;

import dev.goldmensch.fluava.ast.tree.pattern.Pattern;
import dev.goldmensch.fluava.ast.tree.pattern.PatternElement;
import io.github.parseworks.FList;

public record SelectExpression(
        InlineExpression expression,
        Pattern defaultVariant,
        FList<Variant> others
) implements PatternElement.Placeable {
}
