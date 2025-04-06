package cldrgenerator.ast;

import com.palantir.javapoet.CodeBlock;
import io.github.parseworks.FList;

import java.util.List;

public record Condition(
        FList<AndCondition> possibilities
) implements CodeEmitter {
    @Override
    public CodeBlock get() {
        List<CodeBlock> blocks = possibilities.stream()
                .map(CodeEmitter::get)
                .map(block -> CodeBlock.builder().add("(").add(block).add(")").build())
                .toList();

        return CodeBlock.join(blocks, " || ");
    }
}
