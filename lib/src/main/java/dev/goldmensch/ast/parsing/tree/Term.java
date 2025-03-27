package dev.goldmensch.ast.parsing.tree;

import io.github.parseworks.FList;

public record Term(
        String id,
        Pattern pattern,
        FList<Attribute> attributes
) implements Entry{
}
