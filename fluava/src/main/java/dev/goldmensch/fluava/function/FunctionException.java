package dev.goldmensch.fluava.function;

/// An exception that is thrown if anything fails during function execution
public class FunctionException extends RuntimeException {
    public FunctionException(String message) {
        super(message);
    }
}
