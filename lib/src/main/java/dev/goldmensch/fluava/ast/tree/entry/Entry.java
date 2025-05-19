package dev.goldmensch.fluava.ast.tree.entry;

import dev.goldmensch.fluava.ast.tree.Resource;
import dev.goldmensch.fluava.ast.tree.message.Message;

public sealed interface Entry extends Resource.ResourceComponent permits Comment, Message, Term {
}
