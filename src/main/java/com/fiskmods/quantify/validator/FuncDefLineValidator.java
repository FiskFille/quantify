package com.fiskmods.quantify.validator;

import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.insn.InsnNode;
import com.fiskmods.quantify.interpreter.InterpreterStack;

import java.util.LinkedList;

import static com.fiskmods.quantify.insn.Instruction.*;

public class FuncDefLineValidator implements LineValidator {
    private boolean isComplete;

    @Override
    public InsnNode verifyNode(InterpreterStack stack, InsnNode next) throws QtfParseException {
//        if (stack.wasLast(Instruction::isVariable) && next.instruction == NXT) {
//            return next;
//        }
//        if (stack.wasLast(t -> t == NXT)) {
//            if (!isVariable(next.instruction)) {
//                throw stack.unexpectedToken("expected variable");
//            }
//            return next;
//        }
//
//        if (isAssignment(next.instruction)) {
//            hasAssignment = true;
//            stack.recalculateSyntax(null);
//        }
//        else if (stack.lineSize() > 1) {
//            throw stack.unexpectedToken("expected assignment");
//        }
        return next;
    }

    @Override
    public void endLine(InterpreterStack stack, InsnNode last) throws QtfParseException {
//        if (last == null) {
//            return;
//        }
//
//        if (last.instruction == DEF) {
//            if (((MemberInsnNode) last).name == null) {
//                throw stack.error("expected name");
//            }
//        }
//        else if (!hasAssignment) {
//            throw stack.error("line is missing an assignment");
//        }
    }

    public boolean isComplete() {
        return isComplete;
    }

    public static LineValidatorFactory FACTORY = new LineValidatorFactory() {
        @Override
        public boolean isApplicable(InterpreterStack stack, LinkedList<InsnNode> line, LineValidator prev, InsnNode next) {
            return line.isEmpty() && next != null && next.instruction == FDEF
                    || prev instanceof FuncDefLineValidator f && !f.isComplete();
        }

        @Override
        public LineValidator create(LineValidator prev) {
            return new FuncDefLineValidator();
        }
    };
}
