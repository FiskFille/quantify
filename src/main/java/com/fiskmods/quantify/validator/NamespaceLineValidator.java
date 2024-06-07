package com.fiskmods.quantify.validator;

import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.insn.InsnNode;
import com.fiskmods.quantify.interpreter.InterpreterStack;

import java.util.LinkedList;

import static com.fiskmods.quantify.exception.QtfParseException.unexpectedToken;
import static com.fiskmods.quantify.insn.Instruction.NSP;
import static com.fiskmods.quantify.insn.Instruction.isClause;

public class NamespaceLineValidator implements LineValidator {
    @Override
    public InsnNode verifyNode(InterpreterStack stack, InsnNode next) throws QtfParseException {
        if (isClause(next.instruction)) {
            stack.recalculateSyntax(next);
            return next;
        }
        if (stack.lineSize() > 0) {
            throw unexpectedToken(stack.reader, "expected new line");
        }
        return next;
    }

    @Override
    public void endLine(InterpreterStack stack, InsnNode last) {
    }

    public static LineValidatorFactory FACTORY = new LineValidatorFactory() {
        @Override
        public boolean isApplicable(InterpreterStack stack, LinkedList<InsnNode> line, LineValidator prev, InsnNode next) {
            return line.isEmpty() && next != null && next.instruction == NSP;
        }

        @Override
        public LineValidator create(LineValidator prev) {
            return new NamespaceLineValidator();
        }
    };
}
