package com.fiskmods.quantify.assembly;

import com.fiskmods.quantify.exception.QtfAssemblyException;
import com.fiskmods.quantify.insn.InsnNode;
import com.fiskmods.quantify.script.QtfEvaluator;

import java.util.List;

import static com.fiskmods.quantify.insn.Instruction.isExecution;

public enum ExecutionAssembler implements Assembler {
    INSTANCE;

    public static final AssemblerFactory FACTORY = line -> isExecution(line.get(0).instruction) ? INSTANCE : null;

    @Override
    public AssemblyRunnable assemble(List<InsnNode> nodes, AssemblyRunnable.Supplier nextLine, QtfEvaluator evaluator) throws QtfAssemblyException {
        Object result = ExpressionAssembler.assemble(nodes, evaluator);
        if (!(result instanceof AssemblyFunction func)) {
            throw evaluator.error("not a function node", nodes.get(0));
        }
        return func::apply;
    }
}
