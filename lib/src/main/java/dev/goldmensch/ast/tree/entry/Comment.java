package dev.goldmensch.ast.tree.entry;

public record Comment(
        Type type,
        String content
) implements Entry {
    public enum Type {
        SINGLE,
        DOUBLE,
        TRIPLE
    }
}
