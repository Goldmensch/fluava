package dev.goldmensch.fluava.ast.tree.entry;

import dev.goldmensch.fluava.ast.tree.AstResource;
import dev.goldmensch.fluava.ast.tree.message.AstMessage;

public sealed interface Entry extends AstResource.ResourceComponent permits Comment, AstMessage, Term {
}
