package com.fiskmods.quantify.parser;

import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.jvm.JvmFunction;

import java.util.function.Function;

@FunctionalInterface
public interface SyntaxParser<T extends JvmFunction> {
    T accept(QtfParser parser, SyntaxContext context) throws QtfParseException;

    default SyntaxParser<?> or(SyntaxParser<?> other) {
        return (parser, context) -> {
            T result = accept(parser, context);
            return result != null ? result : other.accept(parser, context);
        };
    }

    default <R extends JvmFunction> SyntaxParser<R> map(Function<T, SyntaxParser<R>> func) {
        return (parser, context) -> func.apply(accept(parser, context))
                .accept(parser, context);
    }
}
