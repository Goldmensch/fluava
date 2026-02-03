package dev.goldmensch.fluava.logging;

import dev.goldmensch.fluava.Resource;
import dev.goldmensch.fluava.logging.internal.LogConfigImpl;
import org.slf4j.event.Level;

/// Allows configuration of log levels of some log messages.
public sealed interface LogConfig permits LogConfigImpl {

    /// Configures the logging level for "didn't find key" in [Resource#message(String)]
    ///
    /// Defaults set to [Level#WARN]
    ///
    /// @param logLevel the log level to use
    LogConfig keyNotFound(Level logLevel);

}
