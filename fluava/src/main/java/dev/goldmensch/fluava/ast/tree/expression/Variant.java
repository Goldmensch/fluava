package dev.goldmensch.fluava.ast.tree.expression;

import dev.goldmensch.fluava.ast.tree.pattern.Pattern;

public record Variant(VariantKey key, Pattern pattern) {
    public sealed interface VariantKey permits InlineExpression.NumberLiteral, InlineExpression.StringLiteral {}
}
