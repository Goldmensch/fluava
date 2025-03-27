package dev.goldmensch.ast.parsing.tree;

import dev.goldmensch.ast.parsing.PatternP;
import io.github.parseworks.FList;

public record SelectExpression(
        InlineExpression expression,
        Variant defaultVariant,
        FList<Variant> other
) implements PatternElement.PlaceableExpression {
    public sealed interface VariantKey permits InlineExpression.NumberLiteral, InlineExpression.StringLiteral {}
    public record Variant(VariantKey key, Pattern pattern) {}
}
