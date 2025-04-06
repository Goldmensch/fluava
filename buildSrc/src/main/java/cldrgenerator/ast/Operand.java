package cldrgenerator.ast;

import cldrgenerator.Generator;
import com.palantir.javapoet.CodeBlock;

// see https://unicode.org/reports/tr35/tr35-numbers.html?_gl=1*5djcjz*_ga*MTY1MTQxMjY4NS4xNzQyMjQwODI5*_ga_BPN1D3SEJM*MTc0Mzc4MDEwOS41LjAuMTc0Mzc4MDEwOS4wLjAuMA..#operands
public enum Operand implements CodeEmitter {
    ABSOLUTE_VALUE("n"), // n - the absolute value of N.
    INTEGER_DIGITS("i"), // i - the integer digits of N.
    NUM_FRACTIONS("v"), // v - the number of visible fraction digits in N, with trailing zeros.
    NUM_FRACTIONS_WOZ("w"), // w - the number of visible fraction digits in N, without trailing zeros.
    NUM_FRACTIONS_AS_INT("f"), // f - the visible fraction digits in N, with trailing zeros, expressed as an integer.
    NUM_FRACTIONS_WOZ_AS_INT("t"), // t - the visible fraction digits in N, without trailing zeros, expressed as an integer.
    COMPACT_DEC_EXPO("c"); // c - compact decimal exponent value: exponent of the power of 10 used in compact decimal formatting.

    private final String name;

    Operand(String name) {
        this.name = name;
    }

    @Override
    public CodeBlock get() {
        return CodeBlock.of("$L($L)", name, Generator.VALUE);
    }
}
