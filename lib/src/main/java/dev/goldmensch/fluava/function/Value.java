package dev.goldmensch.fluava.function;

public sealed interface Value {
    sealed interface Result extends Value {
        String stringValue();
    }

    Object value();

    record Raw(Object value) implements Value {}

    record Number(String stringValue, Double value) implements Result {}

    record Text(String stringValue) implements Result {
        @Override
        public String value() {
            return stringValue;
        }
    }
}
