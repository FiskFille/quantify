package com.fiskmods.quantify.validator;

import com.fiskmods.quantify.insn.InsnNode;
import com.fiskmods.quantify.interpreter.InterpreterStack;

import java.util.LinkedList;

public interface LineValidatorFactory {
    boolean isApplicable(InterpreterStack stack, LinkedList<InsnNode> line, LineValidator prev, InsnNode next);

    LineValidator create(LineValidator prev);
}
