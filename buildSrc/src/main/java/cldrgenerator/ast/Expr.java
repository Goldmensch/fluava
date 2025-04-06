package cldrgenerator.ast;

import com.palantir.javapoet.CodeBlock;

public record Expr(
        Operand operand,
        Value mod
) implements CodeEmitter {
    @Override
    public CodeBlock get() {
        CodeBlock.Builder builder = CodeBlock.builder()
                .add("(").add(operand.get());
        if (mod != null) {
            builder.add(" % $L", mod.value());
        }
        builder.add(")");
        return builder.build();
    }
}
