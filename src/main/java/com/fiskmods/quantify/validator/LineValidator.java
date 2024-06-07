package com.fiskmods.quantify.validator;

import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.insn.InsnNode;
import com.fiskmods.quantify.interpreter.InterpreterStack;

public interface LineValidator {
    InsnNode verifyNode(InterpreterStack stack, InsnNode next) throws QtfParseException;

    void endLine(InterpreterStack stack, InsnNode last) throws QtfParseException;
}
