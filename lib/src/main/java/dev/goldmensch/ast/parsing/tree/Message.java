package dev.goldmensch.ast.parsing.tree;

import io.github.parseworks.FList;

import java.util.Optional;

public record Message(String key, Optional<Pattern> value, FList<Attribute> attributes) implements Entry {
}
