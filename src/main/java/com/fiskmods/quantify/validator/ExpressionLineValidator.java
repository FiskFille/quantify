package com.fiskmods.quantify.validator;

import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.insn.InsnNode;
import com.fiskmods.quantify.insn.MemberInsnNode;
import com.fiskmods.quantify.insn.ValueInsnNode;
import com.fiskmods.quantify.interpreter.InterpreterStack;

import java.util.LinkedList;
import java.util.function.Predicate;

import static com.fiskmods.quantify.exception.QtfParseException.error;
import static com.fiskmods.quantify.exception.QtfParseException.unexpectedToken;
import static com.fiskmods.quantify.insn.Instruction.*;
import static com.fiskmods.quantify.util.ScanDirection.LEFT;
import static com.fiskmods.quantify.util.ScanDirection.RIGHT;

public class ExpressionLineValidator implements LineValidator {
    private boolean subtract;
    private boolean ignoreNextSub;
    private int negatives;

    protected int brackets;
    protected boolean inFunction;

    private final Predicate<MemberInsnNode> validRefPredicate;

    public ExpressionLineValidator(int brackets, Predicate<MemberInsnNode> validRefPredicate) {
        this.brackets = brackets;
        this.validRefPredicate = validRefPredicate;
    }

    public boolean isInFunction() {
        return inFunction;
    }

    protected boolean isFirstNode(InterpreterStack stack) {
        return stack.lineSize() == 0;
    }

    protected boolean isValidAfterValue(InsnNode node) {
        return isOperator(node.instruction) || node.instruction == BND;
    }

    @Override
    public InsnNode verifyNode(InterpreterStack stack, InsnNode next) throws QtfParseException {
        if (next.instruction == SUB && ignoreNextSub) {
            ignoreNextSub = false;
            return next;
        }
        if ((next = verifyNegatives(stack, next)) == null) {
            return null;
        }

        InsnNode last = stack.getLast();

        if ((isFirstNode(stack) || !isValueFrom(last.instruction, RIGHT))
                && !isValueFrom(next.instruction, LEFT)) {
            throw unexpectedToken(stack.reader, "expected value");
        }

        if (isValueFrom(last.instruction, RIGHT)) {
            // Implicit multiplication before brackets
            if (next.instruction == BST && (last.instruction == BND || isNumber(last.instruction))) {
                ++brackets;
                stack.add(new InsnNode(MUL));
                return next;
            }

            if (!isValidAfterValue(next)) {
                throw unexpectedToken(stack.reader, "expected operator");
            }
        }

        if (next.instruction == FREF) {
            inFunction = true;
            stack.recalculateSyntax(null);
            return next;
        }

        if (next.instruction == BST) {
            ++brackets;
        }
        else if (next.instruction == BND && --brackets <= 0) {
            return verifyClosingBracket(stack, next);
        }

        if (validRefPredicate != null && next.instruction == REF
                && next instanceof MemberInsnNode var && !validRefPredicate.test(var)) {
            throw error(stack.reader, "definition can't be self-referential");
        }
        return next;
    }

    private InsnNode verifyNegatives(InterpreterStack stack, InsnNode next)
            throws QtfParseException {
        if (next.instruction == SUB) {
            // Skip if already in loop
            if (subtract && negatives <= 0) {
                return next;
            }
            // Determine if this is part of a subtraction at start of loop
            if (negatives == 0) {
                subtract = stack.wasLast(t -> isValueFrom(t, RIGHT));
            }
            ++negatives;
            return null;
        }
        if (negatives <= 0) {
            return next;
        }

        negatives = -negatives;
        if (subtract) {
            stack.add(negatives % 2 == -1 ? SUB : ADD);
        }
        else if (negatives % 2 == -1) {
            if (!isValueFrom(next.instruction, LEFT)) {
                throw unexpectedToken(stack.reader, "expected value");
            }
            if (next.instruction == BST) {
                ignoreNextSub = true;
                stack.add(SUB);
            }
            else {
                next = ValueInsnNode.negated(next);
            }
        }
        negatives = 0;
        subtract = false;
        return next;
    }

    protected InsnNode verifyClosingBracket(InterpreterStack stack, InsnNode next) throws QtfParseException {
        return next;
    }

    @Override
    public void endLine(InterpreterStack stack, InsnNode last) throws QtfParseException {
        if (last != null && !isValueFrom(last.instruction, RIGHT)) {
            throw error(stack.reader, "line must end on a value");
        }
        if (brackets != 0) {
            throw error(stack.reader, "unbalanced brackets");
        }
    }

    public static LineValidatorFactory FACTORY = new LineValidatorFactory() {
        @Override
        public boolean isApplicable(InterpreterStack stack, LinkedList<InsnNode> line, LineValidator prev, InsnNode next) {
            return prev instanceof AssignmentLineValidator a && a.hasAssignment()
                    || prev instanceof ParameterLineValidator p && !p.isInFunction()
                    || prev instanceof ClauseLineValidator;
        }

        @Override
        public LineValidator create(LineValidator prev) {
            return new ExpressionLineValidator(0,
                    prev instanceof AssignmentLineValidator a ? a.getValidRefPredicate() : null);
        }
    };
}
