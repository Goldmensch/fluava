package dev.goldmensch.function;

public sealed interface Value<T> {
    sealed interface Result<T> extends Value<T> {}

    String stringValue();
    T value();

    record Variable(Object value) implements Value<Object> {
        @Override
        public String stringValue() {
            throw new UnsupportedOperationException("Must be converted to string/number by a function call!");
        }
    }

    record Number(String stringValue, Double value) implements Result<Double> {}

    record Text(String stringValue) implements Result<String> {
        @Override
        public String value() {
            return stringValue;
        }
    }
}
