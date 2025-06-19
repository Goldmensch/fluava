package dev.goldmensch.fluava.function;

/// A [Value] ca be one of
///
/// - [Raw], which is just a wrapper around some [Object]
/// - [Number] which is a formatted number
/// - [Text] which is a text
///
/// The last 2 types: [Number] and [Text] represent locale aware formatted values, which can be directly
/// embedded in a message. These values are returned by a [Function].
///
public sealed interface Value<T> {
    /// the underlying object/string/double
    T value();

    /// A wrapper around some [Object]
    /// @param value the underlying value
    record Raw(Object value) implements Value<Object> {}

    /// A formatted value, that can be either [Number] or [Text]
    sealed interface Formatted<T> extends Value<T> {

        /// @return the formatted locale aware string representation, that will be embedded in the message
        String stringValue();
    }

    /// A number value
    ///
    /// @param stringValue the locale specific string representation
    /// @param value the underlying [Double] value
    record Number(String stringValue, Double value) implements Formatted<Double> {}

    /// A text value
    ///
    /// @param stringValue the string that will be embedded in the message
    record Text(String stringValue) implements Formatted<String> {

        /// @return the same as [#stringValue()]
        @Override
        public String value() {
            return stringValue;
        }
    }
}
