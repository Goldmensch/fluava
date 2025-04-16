package dev.goldmensch.ast.tree.pattern;

import io.github.parseworks.FList;

public record Pattern(
        FList<PatternElement> components
)  {
}
