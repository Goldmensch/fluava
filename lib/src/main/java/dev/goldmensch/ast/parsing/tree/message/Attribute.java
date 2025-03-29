package dev.goldmensch.ast.parsing.tree.message;

import dev.goldmensch.ast.parsing.tree.pattern.Pattern;

public record Attribute(
        String id,
        Pattern pattern
) {
}
