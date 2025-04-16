package dev.goldmensch.ast.tree.entry;

import dev.goldmensch.ast.tree.message.Attribute;
import dev.goldmensch.ast.tree.pattern.Pattern;
import io.github.parseworks.FList;

public record Term(
        String id,
        Pattern pattern,
        FList<Attribute> attributes
) implements Entry {
}
