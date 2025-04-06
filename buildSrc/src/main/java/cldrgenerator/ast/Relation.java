package cldrgenerator.ast;

import cldrgenerator.Generator;
import com.palantir.javapoet.CodeBlock;

import java.util.List;

public sealed interface Relation extends CodeEmitter {
    record Not(Relation relation) implements Relation {

        @Override
        public CodeBlock get() {
            return CodeBlock.builder().add("!(").add(relation.get()).add(")").build();
        }
    }

    record In(Expr expr, RangeList rangeList) implements Relation {
        @Override
        public CodeBlock get() {
            CodeBlock exprBlock = expr.get();

            List<CodeBlock> blocks = rangeList.elements().stream()
                    .map(element -> switch (element) {
                        case Value(long value) -> CodeBlock.of("($L == $L)", exprBlock, value);
                        case Range(Value(long from), Value(long to)) ->
                                CodeBlock.of("(isInt($L) && $L <= $L && $L >= $L)", exprBlock, from, exprBlock, to, exprBlock);
                    })
                    .toList();

            return CodeBlock.join(blocks, " || ");
        }
    }

    record Within(Expr expr, RangeList rangeList) implements Relation {
        @Override
        public CodeBlock get() {
            CodeBlock exprBlock = expr.get();

            List<CodeBlock> blocks = rangeList.elements().stream()
                    .map(element -> switch (element) {
                        case Value(long value) -> CodeBlock.of("($L == $L)", exprBlock, value);
                        case Range(Value(long from), Value(long to)) ->
                                CodeBlock.of("$L <= $L && $L >= $L)", Generator.VALUE, from, exprBlock, to, exprBlock);
                    })
                    .toList();

            return CodeBlock.join(blocks, " || ");
        }
    }
}
