package com.fiskmods.quantify.exception;

import com.fiskmods.quantify.lexer.TextScanner;
import com.fiskmods.quantify.lexer.token.Token;
import com.fiskmods.quantify.parser.QtfParser;

public class QtfCompilerException extends QtfException {
    public QtfCompilerException(Throwable cause) {
        super(cause);
    }

    public QtfCompilerException(String message) {
        super(message);
    }

    public static QtfCompilerException handle(QtfLexerException cause) {
        return new QtfCompilerException(cause.getMessage());
    }

    public static QtfCompilerException handle(QtfParser parser, QtfParseException cause, String text) {
        String message = cause.getMessage();
        Token location = cause.location != null ? cause.location : parser.last();
        if (location != null) {
            message += " at " + TextScanner.address(text, location.startIndex());
        }
        if (cause.reason != null) {
            message += " - " + cause.reason;
        }
        if (location != null) {
            message += TextScanner.trace(text, location.startIndex());
        }
        return new QtfCompilerException(message);
    }
}
