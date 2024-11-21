package com.fiskmods.quantify.parser;

import com.fiskmods.quantify.exception.QtfParseException;

@FunctionalInterface
public interface SyntaxParser<T extends SyntaxElement> {
    T accept(QtfParser parser, SyntaxContext context) throws QtfParseException;
}
