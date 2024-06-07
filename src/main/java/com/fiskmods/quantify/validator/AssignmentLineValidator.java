package com.fiskmods.quantify.validator;

import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.insn.InsnNode;
import com.fiskmods.quantify.insn.Instruction;
import com.fiskmods.quantify.insn.MemberInsnNode;
import com.fiskmods.quantify.interpreter.InterpreterStack;

import java.util.LinkedList;
import java.util.function.Predicate;

import static com.fiskmods.quantify.exception.QtfParseException.error;
import static com.fiskmods.quantify.exception.QtfParseException.unexpectedToken;
import static com.fiskmods.quantify.insn.Instruction.*;

public class AssignmentLineValidator implements LineValidator {
    private boolean hasAssignment;
    private Predicate<MemberInsnNode> validRefPredicate;

    @Override
    public InsnNode verifyNode(InterpreterStack stack, InsnNode next) throws QtfParseException {
        if (next.instruction == SUB) {
            if (stack.lineSize() > 1 && !stack.wasLast(t -> isReference(t) || t == NXT)) {
                throw unexpectedToken(stack.reader, "expected variable");
            }
            return next;
        }
        if (stack.wasLast(t -> t == SUB)) {
            if (isReference(next.instruction) && next instanceof MemberInsnNode var) {
                stack.removeLast();
                return var.negated();
            }
            throw unexpectedToken(stack.reader, "expected variable");
        }

        if (stack.wasLast(Instruction::isVariable) && next.instruction == NXT) {
            return next;
        }
        if (stack.wasLast(t -> t == NXT)) {
            if (!isVariable(next.instruction)) {
                throw unexpectedToken(stack.reader, "expected variable");
            }
            if (next.instruction == DEF && next instanceof MemberInsnNode var) {
                validRefPredicate = validRefPredicate.and(t -> t.id != var.id);
            }
            return next;
        }

        if (isAssignment(next.instruction)) {
            if (next.instruction != EQ && stack.wasLast(t -> t == DEF)) {
                throw unexpectedToken(stack.reader, "expected =");
            }
            hasAssignment = true;
            stack.recalculateSyntax(null);
        }
        else if (stack.lineSize() > 1) {
            throw unexpectedToken(stack.reader, "expected assignment");
        }

        if (next.instruction == DEF && next instanceof MemberInsnNode var) {
            validRefPredicate = t -> t.id != var.id;
        }
        return next;
    }

    @Override
    public void endLine(InterpreterStack stack, InsnNode last) throws QtfParseException {
        if (last != null && last.instruction != DEF && !hasAssignment) {
            throw error(stack.reader, "line is missing an assignment");
        }
        validRefPredicate = null;
    }

    public boolean hasAssignment() {
        return hasAssignment;
    }

    public Predicate<MemberInsnNode> getValidRefPredicate() {
        return validRefPredicate;
    }

    public static LineValidatorFactory FACTORY = new LineValidatorFactory() {
        @Override
        public boolean isApplicable(InterpreterStack stack, LinkedList<InsnNode> line, LineValidator prev, InsnNode next) {
            return line.isEmpty() && next != null && (isAssignable(next.instruction) || next.instruction == SUB);
        }

        @Override
        public LineValidator create(LineValidator prev) {
            return new AssignmentLineValidator();
        }
    };
}
