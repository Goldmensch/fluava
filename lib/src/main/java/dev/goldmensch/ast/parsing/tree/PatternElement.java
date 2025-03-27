package dev.goldmensch.ast.parsing.tree;

public sealed interface PatternElement {
    sealed interface PlaceableExpression permits InlineExpression, SelectExpression {}

    record Text(String message) implements PatternElement {}
    record Placeable(PlaceableExpression expression) implements PatternElement, InlineExpression {}
}
