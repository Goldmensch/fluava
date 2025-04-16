package dev.goldmensch.ast.tree.entry;

import dev.goldmensch.ast.tree.Resource;
import dev.goldmensch.ast.tree.message.Message;

public sealed interface Entry extends Resource.ResourceComponent permits Comment, Message, Term {
}
