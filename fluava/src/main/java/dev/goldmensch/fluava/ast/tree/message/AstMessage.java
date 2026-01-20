package dev.goldmensch.fluava.ast.tree.message;

import dev.goldmensch.fluava.ast.tree.entry.Entry;
import dev.goldmensch.fluava.ast.tree.pattern.Pattern;
import io.github.parseworks.FList;

import java.util.Optional;

public record AstMessage(String id, Optional<Pattern> content, FList<Attribute> attributes, Optional<String> comment) implements Entry {
}
