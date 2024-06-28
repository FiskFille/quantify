package com.fiskmods.quantify.assembly;

import com.fiskmods.quantify.exception.QtfAssemblyException;
import com.fiskmods.quantify.insn.InsnNode;
import com.fiskmods.quantify.jvm.JvmFunction;
import com.fiskmods.quantify.script.QtfEvaluator;

import java.util.List;

@FunctionalInterface
public interface Assembler {
    JvmFunction assemble(List<InsnNode> nodes, JvmFunction.Supplier nextLine, QtfEvaluator evaluator) throws QtfAssemblyException;
}
