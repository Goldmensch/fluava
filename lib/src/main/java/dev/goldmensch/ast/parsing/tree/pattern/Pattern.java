package dev.goldmensch.ast.parsing.tree.pattern;

import io.github.parseworks.FList;

public record Pattern(
        FList<PatternElement> components
)  {
}
