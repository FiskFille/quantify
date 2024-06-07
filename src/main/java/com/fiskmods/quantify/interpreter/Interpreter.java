package com.fiskmods.quantify.interpreter;

import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.insn.Instruction;
import com.fiskmods.quantify.util.TokenReader;

import static com.fiskmods.quantify.insn.Instruction.NXT;

@FunctionalInterface
public interface Interpreter {
    boolean interpret(TokenReader reader, InterpreterStack stack) throws QtfParseException;

    Interpreter BRACKETS = (reader, stack) -> {
        char c = reader.nextChar();
        if (c == '(') {
            stack.add(Instruction.BST);
            return true;
        }
        if (c == ')') {
            stack.add(Instruction.BND);
            return true;
        }
        return false;
    };

    Interpreter COMMA = (reader, stack) -> {
        if (reader.nextChar() == ',') {
            stack.add(NXT);
            return true;
        }
        return false;
    };
}
