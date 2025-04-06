package cldrgenerator.ast;

import com.palantir.javapoet.CodeBlock;

import java.util.function.Supplier;

public sealed interface CodeEmitter extends Supplier<CodeBlock> permits AndCondition, Condition, Expr, Operand, Relation {
}
