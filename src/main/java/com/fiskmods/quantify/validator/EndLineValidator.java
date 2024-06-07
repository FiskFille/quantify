package com.fiskmods.quantify.validator;

import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.insn.InsnNode;
import com.fiskmods.quantify.interpreter.InterpreterStack;

import java.util.LinkedList;

import static com.fiskmods.quantify.insn.Instruction.END;

public class EndLineValidator implements LineValidator {
    @Override
    public InsnNode verifyNode(InterpreterStack stack, InsnNode next) throws QtfParseException {
        if (next.instruction != END) {
            throw QtfParseException.unexpectedToken(stack.reader, "expected end");
        }
        return next;
    }

    @Override
    public void endLine(InterpreterStack stack, InsnNode last) {
    }

    public static LineValidatorFactory FACTORY = new LineValidatorFactory() {
        @Override
        public boolean isApplicable(InterpreterStack stack, LinkedList<InsnNode> line, LineValidator prev, InsnNode next) {
            return line.isEmpty() && next != null && next.instruction == END;
        }

        @Override
        public LineValidator create(LineValidator prev) {
            return new EndLineValidator();
        }
    };
}
