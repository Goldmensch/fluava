package dev.goldmensch.fluava.ast.tree.pattern;

import io.github.parseworks.FList;

public record Pattern(
        FList<PatternElement> components
)  {
}
