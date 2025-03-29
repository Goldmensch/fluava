package dev.goldmensch.ast.parsing.tree.expression;

import dev.goldmensch.ast.parsing.tree.pattern.Pattern;

public record Variant(VariantKey key, Pattern pattern) {
    public sealed interface VariantKey permits InlineExpression.NumberLiteral, InlineExpression.StringLiteral {}
}
