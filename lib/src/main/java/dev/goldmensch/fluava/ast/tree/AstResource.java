package dev.goldmensch.fluava.ast.tree;

import dev.goldmensch.fluava.ast.tree.entry.Entry;
import io.github.parseworks.FList;

public record AstResource(
        FList<? extends ResourceComponent> components
) {
    public sealed interface ResourceComponent permits Entry, ResourceComponent.Blank, ResourceComponent.Junk {
        record Blank() implements ResourceComponent {}
        record Junk(String content) implements ResourceComponent {}
    }
}
