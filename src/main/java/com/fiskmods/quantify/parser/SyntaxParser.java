package com.fiskmods.quantify.parser;

import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.jvm.JvmFunction;

@FunctionalInterface
public interface SyntaxParser<T extends JvmFunction> {
    T accept(QtfParser parser, SyntaxContext context) throws QtfParseException;
}
