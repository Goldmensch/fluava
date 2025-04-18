package dev.goldmensch.ast.tree;

import dev.goldmensch.ast.tree.entry.Entry;
import io.github.parseworks.FList;

public record Resource(
        FList<? extends ResourceComponent> components
) {
    public sealed interface ResourceComponent permits Entry, ResourceComponent.Blank, ResourceComponent.Junk {
        record Blank() implements ResourceComponent {}
        record Junk(String content) implements ResourceComponent {}
    }
}
