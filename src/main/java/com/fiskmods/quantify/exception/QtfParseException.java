package com.fiskmods.quantify.exception;

import com.fiskmods.quantify.lexer.token.Token;

public class QtfParseException extends QtfException {
    public final String reason;
    public final Token location;

    public QtfParseException(String message, String reason, Token location) {
        super(message);
        this.reason = reason;
        this.location = location;
    }

    public QtfParseException(String message) {
        super(message);
        this.reason = null;
        this.location = null;
    }

    public QtfParseException(Throwable cause) {
        this(cause.getMessage());
    }

    public static QtfParseException internal(String reason, Token location) {
        return new QtfParseException("Internal error", reason, location);
    }

    public static QtfParseException error(String reason, Token location) {
        return new QtfParseException("Unresolved error", reason, location);
    }
}
