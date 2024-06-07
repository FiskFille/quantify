package com.fiskmods.quantify.validator;

import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.insn.InsnNode;
import com.fiskmods.quantify.interpreter.InterpreterStack;

import java.util.LinkedList;

import static com.fiskmods.quantify.insn.Instruction.IF;
import static com.fiskmods.quantify.insn.Instruction.WTH;

public class ClauseLineValidator implements LineValidator {
    @Override
    public InsnNode verifyNode(InterpreterStack stack, InsnNode next) throws QtfParseException {
        stack.recalculateSyntax(null);
        return next;
    }

    @Override
    public void endLine(InterpreterStack stack, InsnNode last) {
    }

    public static LineValidatorFactory FACTORY = new LineValidatorFactory() {
        @Override
        public boolean isApplicable(InterpreterStack stack, LinkedList<InsnNode> line, LineValidator prev, InsnNode next) {
            return line.isEmpty() && next != null && switch (next.instruction) {
                case IF, WTH -> true;
                default -> false;
            };
        }

        @Override
        public LineValidator create(LineValidator prev) {
            return new ClauseLineValidator();
        }
    };
}
