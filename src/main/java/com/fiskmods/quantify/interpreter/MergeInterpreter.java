package com.fiskmods.quantify.interpreter;

import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.util.TokenReader;

import static com.fiskmods.quantify.exception.QtfParseException.unexpectedToken;

public enum MergeInterpreter implements Interpreter {
    INSTANCE;

    @Override
    public boolean interpret(TokenReader reader, InterpreterStack stack) throws QtfParseException {
        if (reader.nextChar() != '|') {
            return false;
        }
        if (stack.lineSize() > 0) {
            throw unexpectedToken(reader, "line merge must happen at the start of a line");
        }
        return true;
    }
}
