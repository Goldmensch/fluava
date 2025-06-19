package dev.goldmensch.fluava.function;

import java.util.Map;

/// Allows to add predefined named arguments to the default (implicit) function of a certain value.
/// Fluent refers to this sort of functions as "partial functions".
///
///
/// ## Usage Example
/// ```java
/// User user = getUser();
/// Bundle bundle = fluava.loadBundle("app");
/// String msg = bundle.apply(Locale.GERMAN, "fail", Map.of("user", new Partial(user, Map.of("task", "none"))));
/// ```
///
/// The code above applies the named argument "task" with value "none" to the default (implicit) function of the class
/// of `User`.
/// @param params the predefined named arguments
/// @param value the variables value
public record Partial(
        Object value,
        Map<String, Object> params
) {
}
