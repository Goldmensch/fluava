# Fluava
A modern, fast java implementation of the amazing [project fluent](https://projectfluent.org/) ☕

## Motivation
I started this project due to the need for a lightweight localization framework for a project
called [JDA-Commands](https://github.com/Kaktushose/jda-commands).
Existing solutions for the java ecosystem were either integrated into large web frameworks
like Spring or Micronaut or massive like ICU.

So, after doing a bit of research I stumbled across [project fluent](https://projectfluent.org/) and
was amazed by its expressiveness and simplicity. 
And that's how this library was born.

## Dependency
Fluava is published on maven central.

`gradle.build.kts`
```kotlin
implementation("dev.goldmensch:fluava:VERSION")
```

## Usage
Important! I'm not going to explain how fluent works in general. This guide is primary
about how this library implements the fluent functionality. Knowledge about the project's
syntax and concepts are required.

To start using this library, you have to create an instance of the `Fluava` class by 
calling `Fluava#create(Locale)` or `Fluava#builder(Locale)`.

The `Fluava` class serves as the main entry point to the library, providing several 
helpful methods to create and load resource and bundles.

### Resources
Resources are in general a collection of fluent files belonging together based on 
the locale and basename.

The fluents file content are structured according to the [Guide here](https://projectfluent.org/fluent/guide/).

They can be loaded from raw strings by calling `Fluava#of(String, Locale)` or from disk by calling
`Fluava#of(Path, Locale)`. In that case the resulting `Resource` of course only consists of this one file.

### Bundles
Bundles are a collection of multiple resources with the same basename but different locales.
They can be loaded by calling `Fluava#loadBundle(String)` and the basename as the parameter.

The load process of Bundles are currently limited to the classpath and very similar to how java's
ResourceBundles work but with a more flexible directory structure.
The classpath will be searched lazily for a fluent file given a specific locale according to following order:

 1. BASE_LANGUAGE_COUNTRY_VARIANT.ftl
 2. BASE/LANGUAGE_COUNTRY_VARIANT.ftl
 3. BASE_LANGUAGE_COUNTRY.ftl
 4. BASE/LANGUAGE_COUNTRY.ftl
 5. BASE_LANGUAGE.ftl
 6. BASE/LANGUAGE.ftl

If a key isn't found for a specific locale in the whole bundle, then the fallback locale
will be queried for this key.

### Messages
Messages are the smallest entity of this system. They represent the direct localization message 
associated with a key. The in code representation is the `Message` class and provide
methods for getting the messages content and attributes. That class also supports replacing
fluent placeables with their real world value.

### Code Example

```java
// create Fluava instance with englisch as the fallback locale
Fluava fluava = Fluava.create(Locale.ENGLISH);

// load a bundle with the basename "app"
Bundle app = fluent.loadBundle("app");

// just some user to demonstrate 
User user = getUser();

// get the message with key "greet" for the users locale and the variable "name" set to the user's name.
String msg = app.apply(user.locale(), "greet", Map.of("name", user.name()));

// display the localized message to the user
user.display(msg);
```

### Variable formatting and functions
Project fluent introduces so-called "function" to format certain variable 
values. These functions can take multiple positional and named arguments and return
either a number or a text. 

#### Implicit functions
Fluava introduces a subtype of functions called "implicit functions". They represent the
default formatters for certain objects (based on their class) and can be implemented
with the interface `Function.Implicit`.

If in a fluent messages a variables is referenced without an enclosing function. The implicit
function for this variables will be searched and called.

Often, there are implicit functions that support classes that are modeling data very similar to those used in your application.
Just think about a custom "Date" class you receive from the database. Now to include
this "Date" class in an i18n message you normally would need to manually convert
this to some built-in supported one or write a custom function for it. 
But for your luck, Fluava supports [Proteus](https://github.com/Kaktushose/proteus),
which is a universal java type adapting library. 

#### Proteus support
Fluava will try to adapt your value to some supported class, for which an implicit function
exists using the global proteus mapper `Proteus#global()`.

That comes in handy if you have your own types that can be easily converted to some supported ones.
For the example above, you just have to register a proteus mapper that converts
your custom `Data` to for example `java.time.LocalDate` and fluava will just
use the default implicit datetime function with all it's options supported.
For many types this way is way more comfortable than implemented a dedicated function
each time.

#### Partial functions
Project fluent also introduces the term "partial function", which means that
the developer (caller of `Message#apply`) preset some named arguments (options) 
with values. To do that in Fluava you have to wrap your variables value in a `Partial`
record:
```java
User user = getUser();
String msg = app.apply(user.locale(), "current-time", Map.of("time", 
        new Partial(user.getTime(), Map.of("timeZone", "CET"))));
```

It's important to note that such predefined options only apply to the variables
implicit function. In this example this would be the builtin "datetime" function.

With the partial function in the above example, the named argument `timeZone`
is set to the value `CET` by default. This can be overridden if this exact
function is called in the fluent message with this option set there.

