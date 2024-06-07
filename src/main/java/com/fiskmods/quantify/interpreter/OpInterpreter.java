package com.fiskmods.quantify.interpreter;

import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.util.TokenReader;

import static com.fiskmods.quantify.insn.Instruction.*;

public enum OpInterpreter implements Interpreter {
    INSTANCE;

    @Override
    public boolean interpret(TokenReader reader, InterpreterStack stack) throws QtfParseException {
        char c = reader.nextChar();
        int op = fromChar(c);

        if (op != -1) {
            // Repeating char for && and ||
            if ((op == AND || op == OR) && reader.nextChar() != c) {
                return false;
            }
            stack.add(op);
            return true;
        }
        switch (c) {
            case '>' -> {
                if (reader.hasNext() && reader.peekChar() == '=') {
                    stack.add(GEQ);
                    reader.skip(1);
                    return true;
                }
                stack.add(GT);
                return true;
            }
            case '<' -> {
                if (reader.hasNext() && reader.peekChar() == '=') {
                    stack.add(LEQ);
                    reader.skip(1);
                    return true;
                }
                stack.add(LT);
                return true;
            }
            case '=' -> {
                if (reader.hasNext() && reader.peekChar() == '=') {
                    stack.add(EQS);
                    reader.skip(1);
                    return true;
                }
            }
            case '!' -> {
                if (reader.hasNext() && reader.peekChar() == '=') {
                    stack.add(NEQ);
                    reader.skip(1);
                    return true;
                }
            }
        }
        return false;
    }

    public static int fromChar(char c) {
        return switch (c) {
            case '+' -> ADD;
            case '-' -> SUB;
            case '*' -> MUL;
            case '/' -> DIV;
            case '^' -> POW;
            case '%' -> MOD;

            case '&' -> AND;
            case '|' -> OR;
            default -> -1;
        };
    }
}
