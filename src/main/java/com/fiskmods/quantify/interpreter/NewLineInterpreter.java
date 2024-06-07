package com.fiskmods.quantify.interpreter;

import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.util.TokenReader;

public enum NewLineInterpreter implements Interpreter {
    INSTANCE;

    @Override
    public boolean interpret(TokenReader reader, InterpreterStack stack) throws QtfParseException {
        if (reader.nextChar() != '\n' || !reader.hasNext()) {
            return false;
        }
        reader.nextPhrase(c -> c == ' ' || c == '\n' || c == '\t');

        // Skip if | is present
        if (reader.peekChar() == '|') {
            reader.skip(1);
            return true;
        }
        stack.newLine();
        return true;
    }
}
