package dev.goldmensch.ast.parsing.tree.message;

import dev.goldmensch.ast.parsing.tree.entry.Entry;
import dev.goldmensch.ast.parsing.tree.pattern.Pattern;
import io.github.parseworks.FList;

import java.util.Optional;

public record Message(String id, Optional<Pattern> content, FList<Attribute> attributes) implements Entry {
}
