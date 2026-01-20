package dev.goldmensch.fluava.ast.tree.entry;

import dev.goldmensch.fluava.ast.tree.message.Attribute;
import dev.goldmensch.fluava.ast.tree.pattern.Pattern;
import io.github.parseworks.FList;

import java.util.Optional;

public record Term(
        String id,
        Pattern pattern,
        FList<Attribute> attributes,
        Optional<String> comment
) implements Entry {
}
