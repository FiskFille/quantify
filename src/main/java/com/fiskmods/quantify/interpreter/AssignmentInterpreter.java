package com.fiskmods.quantify.interpreter;

import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.insn.Instruction;
import com.fiskmods.quantify.util.TokenReader;

import static com.fiskmods.quantify.exception.QtfParseException.invalidToken;

public enum AssignmentInterpreter implements Interpreter {
    STRICT, LENIENT;

    @Override
    public boolean interpret(TokenReader reader, InterpreterStack stack) throws QtfParseException {
        char c = reader.nextChar();
        if (c == '=') {
            stack.add(Instruction.EQ);
            return true;
        }
        if (reader.hasNext() && reader.nextChar() != '=') {
            return false;
        }
        int op = OpInterpreter.fromChar(c);
        if (op == -1) {
            return false;
        }

        // Don't allow assignment operators in strict mode
        if (this == STRICT) {
            throw invalidToken(stack.reader, "assignment operator not allowed");
        }
        stack.add(Instruction.toAssignment(op));
        return true;
    }
}
