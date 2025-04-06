package cldrgenerator.ast;

import java.util.SequencedCollection;

public record RangeList(
        SequencedCollection<Element> elements
) {

    public sealed interface Element permits Range, Value {}
}
