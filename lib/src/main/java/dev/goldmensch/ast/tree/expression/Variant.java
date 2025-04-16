package dev.goldmensch.ast.tree.expression;

import dev.goldmensch.ast.tree.pattern.Pattern;

public record Variant(VariantKey key, Pattern pattern) {
    public sealed interface VariantKey permits InlineExpression.NumberLiteral, InlineExpression.StringLiteral {}
}
