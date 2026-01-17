package dev.goldmensch.fluava.test.parser;

import io.github.parseworks.Parser;
import io.github.parseworks.Result;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

public class Helper {

    @SuppressWarnings("unchecked")
    public static <T> Result<Character, T> parse(String klass, String field, String content) {
        return (Result<Character, T>) get(klass, field, Parser.class).parseAll(content);
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(String klass, String field, Class<T> type) {
        try {
            // guarantee right initialization order
            Class.forName("dev.goldmensch.fluava.ast.FluentParser");

            Class<?> recv = Class.forName("dev.goldmensch.fluava." + klass);
            MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(recv, MethodHandles.lookup());
            VarHandle handle = lookup.findStaticVarHandle(recv, field, type);

            return (T) handle.get();
        } catch (NoSuchFieldException | IllegalAccessException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
