package dev.goldmensch.ast.parsing.tree;

import io.github.parseworks.FList;

public record Resource(
        FList<? extends Entry> entries
) {
}
