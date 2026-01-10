package dev.goldmensch.fluava;

import dev.goldmensch.fluava.function.Function;
import dev.goldmensch.fluava.function.internal.FunctionConfigImpl;

import java.util.Locale;
import java.util.function.Consumer;

/// A builder used to create instances of [Fluava].
///
/// The default value for the fallback locale is `Locale.ENGLISH`.
public final class FluavaBuilder {
    private String bundleRoot = null;
    private final FunctionConfigImpl functionConfig;
    private Locale fallback = Locale.ENGLISH;

    FluavaBuilder() {
        this.functionConfig = new FunctionConfigImpl();
    }

    FluavaBuilder(Fluava fluava) {
        this.functionConfig = fluava.functionConfig();
        this.fallback = fluava.fallback();
    }

    /// Sets the fallback locale to use when no message is found for a locale.
    /// Defaults to `Locale.ENGLISH`.
    ///
    /// @param fallback the [Locale] to use as the fallback
    /// @return this instance of [FluavaBuilder]
    public FluavaBuilder fallback(Locale fallback) {
        this.fallback = fallback;
        return this;
    }

    /// Allows to configure the functionality related to [Function]s.
    ///
    /// @param consumer the configuration
    /// @return this instance of [FluavaBuilder]
    public FluavaBuilder functions(Consumer<FunctionConfig> consumer) {
        consumer.accept(functionConfig);
        return this;
    }

    /// Sets the "root" package that [Bundle]s should be loaded from.
    ///
    /// If you set this to "localization", the bundle "msg" will be loaded from "localization/msg".
    ///
    /// @param path the root path to be set
    /// @return this instance of [FluavaBuilder]
    ///
    /// @see Bundle
    public FluavaBuilder bundleRoot(String path) {
        bundleRoot = path;
        return this;
    }

    /// Builds a new [Fluava] instance based on the configuration made in this builder.
    ///
    /// @return the [Fluava] instance
    public Fluava build() {
        return new Fluava(
                bundleRoot,
                fallback,
                functionConfig
        );
    }

    /// A configuration for the general functionality of functions
    public interface FunctionConfig {

        /// Registers a [Function] identified by the given name.
        /// This will override any function with the same name.
        ///
        /// @param name the name of function
        /// @param function the [Function] to be registered
        ///
        /// @return this instance of [FunctionConfig]
        FunctionConfig register(String name, Function<?, ?> function);


        /// Registers a [Function] identified by the given name.
        ///
        /// @param name the name of function
        /// @param function the [Function] to be registered
        /// @param override whether to override existing functions associated with this name
        ///
        /// @return this instance of [FunctionConfig]
        FunctionConfig register(String name, Function<?, ?> function, boolean override);


        /// Sets whether the library should fall back to [Object#toString()] if type adapting via proteus isn't possible for a value.
        ///
        /// Defaults to false due to security concerns.
        ///
        /// @param fallback whether to fall back to [Object#toString()
        ///
        /// @return this instance of [FunctionConfig]
        FunctionConfig fallbackToString(boolean fallback);
    }
}
