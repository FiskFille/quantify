package com.fiskmods.quantify.validator;

import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.insn.InsnNode;
import com.fiskmods.quantify.interpreter.InterpreterStack;

import java.util.LinkedList;

import static com.fiskmods.quantify.exception.QtfParseException.error;
import static com.fiskmods.quantify.exception.QtfParseException.unexpectedToken;
import static com.fiskmods.quantify.insn.Instruction.BST;
import static com.fiskmods.quantify.insn.Instruction.NXT;

public class ParameterLineValidator extends ExpressionLineValidator {
    private final boolean isStart;

    private boolean firstNode = true;

    public ParameterLineValidator(boolean isStart, int brackets) {
        super(brackets, null);
        this.isStart = isStart;
        inFunction = true;
    }

    @Override
    protected boolean isFirstNode(InterpreterStack stack) {
        return firstNode || super.isFirstNode(stack);
    }

    @Override
    protected boolean isValidAfterValue(InsnNode node) {
        return super.isValidAfterValue(node) || node.instruction == NXT;
    }

    @Override
    public InsnNode verifyNode(InterpreterStack stack, InsnNode next) throws QtfParseException {
        if (isStart && firstNode && next.instruction != BST) {
            throw unexpectedToken(stack.reader, "expected '('");
        }
        if ((next = super.verifyNode(stack, next)) == null) {
            return null;
        }

        if (next.instruction == NXT) {
            // Don't allow double commas
            if (isFirstNode(stack)) {
                throw unexpectedToken(stack.reader, "expected value");
            }
            stack.recalculateSyntax(null);
        }
        firstNode = false;
        return next;
    }

    @Override
    protected InsnNode verifyClosingBracket(InterpreterStack stack, InsnNode next) throws QtfParseException {
        // Don't allow trailing commas
        if (firstNode) {
            throw unexpectedToken(stack.reader, "expected value");
        }
        inFunction = false;
        stack.recalculateSyntax(null);
        return next;
    }

    @Override
    public void endLine(InterpreterStack stack, InsnNode last) throws QtfParseException {
        super.endLine(stack, last);
        throw error(stack.reader, "cannot end line on a parameter");
    }

    public static LineValidatorFactory FACTORY = new LineValidatorFactory() {
        @Override
        public boolean isApplicable(InterpreterStack stack, LinkedList<InsnNode> line, LineValidator prev, InsnNode next) {
            return prev instanceof ExpressionLineValidator e && e.isInFunction()
                    || prev instanceof ExecutionLineValidator ex && ex.hasParameters();
        }

        @Override
        public LineValidator create(LineValidator prev) {
            if (prev instanceof ExpressionLineValidator e) {
                return new ParameterLineValidator(!(prev instanceof ParameterLineValidator), e.brackets);
            }
            return new ParameterLineValidator(false, 1);
        }
    };
}
