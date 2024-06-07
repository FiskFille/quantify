package com.fiskmods.quantify.validator;

import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.insn.InsnNode;
import com.fiskmods.quantify.interpreter.InterpreterStack;

import java.util.LinkedList;

import static com.fiskmods.quantify.exception.QtfParseException.error;
import static com.fiskmods.quantify.exception.QtfParseException.unexpectedToken;
import static com.fiskmods.quantify.insn.Instruction.*;

public class ExecutionLineValidator implements LineValidator {
    private boolean hasParameters;

    @Override
    public InsnNode verifyNode(InterpreterStack stack, InsnNode next) throws QtfParseException {
        if (next.instruction == BST) {
            hasParameters = true;
            stack.recalculateSyntax(null);
        }
        else if (stack.lineSize() > 1) {
            throw unexpectedToken(stack.reader, "expected parameters");
        }
        return next;
    }

    @Override
    public void endLine(InterpreterStack stack, InsnNode last) throws QtfParseException {
        if (last != null && !hasParameters) {
            if (last.instruction == FRUN) {
                // FRUN instruction doesn't use any parameters or brackets
                return;
            }
            throw error(stack.reader, "function is missing parameters");
        }
    }

    public boolean hasParameters() {
        return hasParameters;
    }

    public static LineValidatorFactory FACTORY = new LineValidatorFactory() {
        @Override
        public boolean isApplicable(InterpreterStack stack, LinkedList<InsnNode> line, LineValidator prev, InsnNode next) {
            return line.isEmpty() && next != null && isExecution(next.instruction);
        }

        @Override
        public LineValidator create(LineValidator prev) {
            return new ExecutionLineValidator();
        }
    };
}
