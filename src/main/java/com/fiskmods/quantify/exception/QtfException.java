package com.fiskmods.quantify.exception;

public class QtfException extends Exception {
    public QtfException(String message) {
        super(message);
    }

    public QtfException(Throwable cause) {
        super(cause);
    }

    public QtfException(String message, Throwable cause) {
        super(message, cause);
    }
}
