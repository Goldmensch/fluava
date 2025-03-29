package dev.goldmensch.ast.parsing.tree.entry;

public record CommentLine(
        Type type,
        String line
) implements Entry {
    public enum Type {
        SINGLE,
        DOUBLE,
        TRIPLE
    }
}
