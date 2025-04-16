package dev.goldmensch.ast.tree.message;

import dev.goldmensch.ast.tree.pattern.Pattern;

public record Attribute(
        String id,
        Pattern pattern
) {
}
