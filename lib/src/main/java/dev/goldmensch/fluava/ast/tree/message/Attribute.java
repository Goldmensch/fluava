package dev.goldmensch.fluava.ast.tree.message;

import dev.goldmensch.fluava.ast.tree.pattern.Pattern;

public record Attribute(
        String id,
        Pattern pattern
) {
}
