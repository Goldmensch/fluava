package dev.goldmensch.ast.parsing.tree.expression;

import dev.goldmensch.ast.parsing.tree.pattern.Pattern;
import dev.goldmensch.ast.parsing.tree.pattern.PatternElement;
import io.github.parseworks.FList;

public record SelectExpression(
        InlineExpression expression,
        Pattern defaultVariant,
        FList<Variant> others
) implements PatternElement.Placeable {
}
