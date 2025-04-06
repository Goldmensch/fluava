package cldrgenerator.ast;

public record Range(
        Value from,
        Value to
) implements RangeList.Element {
}
