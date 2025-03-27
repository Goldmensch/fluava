package dev.goldmensch.ast.parsing.tree;

import io.github.parseworks.FList;

public record Pattern(
        FList<PatternElement> elements
)  {
}
