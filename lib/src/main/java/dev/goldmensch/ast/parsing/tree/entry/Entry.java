package dev.goldmensch.ast.parsing.tree.entry;

import dev.goldmensch.ast.parsing.tree.Resource;
import dev.goldmensch.ast.parsing.tree.message.Message;

public sealed interface Entry extends Resource.ResourceComponent permits CommentLine, Message, Term {
}
