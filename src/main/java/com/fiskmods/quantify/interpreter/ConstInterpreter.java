package com.fiskmods.quantify.interpreter;

import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.insn.CstInsnNode;
import com.fiskmods.quantify.insn.InsnNode;
import com.fiskmods.quantify.insn.Instruction;
import com.fiskmods.quantify.util.TokenReader;

public enum ConstInterpreter implements Interpreter {
    INSTANCE;

    @Override
    public boolean interpret(TokenReader reader, InterpreterStack stack) throws QtfParseException {
        char c = reader.nextChar();
        if (c != '.' && !Character.isDigit(c)) {
            return false;
        }

        String s;
        final boolean[] comma = {c == '.'};
        if (reader.hasNext()) {
            s = reader.nextPhrase(t -> {
                if (t != '.') {
                    return Character.isDigit(t);
                }
                if (comma[0]) {
                    return false;
                }
                comma[0] = true;
                return true;
            });
            s = s != null ? c + s : String.valueOf(c);
        }
        else {
            s = String.valueOf(c);
        }

        try {
            double d;

            // Don't include a trailing comma
            if (comma[0] && s.charAt(s.length() - 1) == '.') {
                // Skip if there's nothing on the left side of the comma
                if (s.length() == 1) {
                    return false;
                }
                d = Double.parseDouble(s.substring(0, s.length() - 1));
                reader.skip(-1);
            }
            else {
                d = Double.parseDouble(s);

                // Degree -> radian conversion
                if (reader.hasNext() && reader.peekChar() == '\'') {
                    d *= Math.PI / 180;
                    reader.skip(1);
                }
            }

            stack.add(createNode(d));
            return true;
        }
        catch (NumberFormatException e) {
            throw new QtfParseException(e);
        }
    }

    public static InsnNode createNode(double value) {
        if (value == 0) {
            return new InsnNode(Instruction.C_0);
        }
        if (value == 1) {
            return new InsnNode(Instruction.C_1);
        }
        if (value == 2) {
            return new InsnNode(Instruction.C_2);
        }
        if (value == 3) {
            return new InsnNode(Instruction.C_3);
        }
        if (value == 4) {
            return new InsnNode(Instruction.C_4);
        }
        if (value == 5) {
            return new InsnNode(Instruction.C_5);
        }
        return new CstInsnNode(value);
    }
}
