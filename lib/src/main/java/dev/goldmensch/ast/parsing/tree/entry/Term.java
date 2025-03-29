package dev.goldmensch.ast.parsing.tree.entry;

import dev.goldmensch.ast.parsing.tree.message.Attribute;
import dev.goldmensch.ast.parsing.tree.pattern.Pattern;
import io.github.parseworks.FList;

public record Term(
        String id,
        Pattern pattern,
        FList<Attribute> attributes
) implements Entry {
}
