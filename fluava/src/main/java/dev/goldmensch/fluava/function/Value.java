package dev.goldmensch.fluava.function;

public sealed interface Value {
    Object value();

    record Raw(Object value) implements Value {}

    sealed interface Formatted extends Value {
        String stringValue();
    }

    record Number(String stringValue, Double value) implements Formatted {}

    record Text(String stringValue) implements Formatted {
        @Override
        public String value() {
            return stringValue;
        }
    }
}
