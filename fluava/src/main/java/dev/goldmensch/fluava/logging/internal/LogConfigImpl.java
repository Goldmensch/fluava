package dev.goldmensch.fluava.logging.internal;

import dev.goldmensch.fluava.logging.LogConfig;
import org.slf4j.event.Level;

public final class LogConfigImpl implements LogConfig {

    private Level keyNotFound = Level.WARN;

    @Override
    public LogConfig keyNotFound(Level logLevel) {
        this.keyNotFound = logLevel;
        return this;
    }

    public Level keyNotFound() {
        return keyNotFound;
    }
}
