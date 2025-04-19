package dev.goldmensch.function;

public sealed interface Value {
    sealed interface Result extends Value {}

    String stringValue();
    Object value();

    record Raw(Object value) implements Value {
        @Override
        public String stringValue() {
            throw new UnsupportedOperationException("Must be converted to string/number by a function call!");
        }
    }

    record Number(String stringValue, Double value) implements Result {}

    record Text(String stringValue) implements Result {
        @Override
        public String value() {
            return stringValue;
        }
    }
}
