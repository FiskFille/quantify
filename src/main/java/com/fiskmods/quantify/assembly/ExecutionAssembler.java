package com.fiskmods.quantify.assembly;

import com.fiskmods.quantify.exception.QtfAssemblyException;
import com.fiskmods.quantify.insn.InsnNode;
import com.fiskmods.quantify.jvm.JvmFunction;
import com.fiskmods.quantify.script.QtfEvaluator;
import org.objectweb.asm.Opcodes;

import java.util.List;

import static com.fiskmods.quantify.insn.Instruction.isExecution;

public enum ExecutionAssembler implements Assembler {
    INSTANCE;

    public static final AssemblerFactory FACTORY = line -> isExecution(line.get(0).instruction) ? INSTANCE : null;

    @Override
    public JvmFunction assemble(List<InsnNode> nodes, JvmFunction.Supplier nextLine, QtfEvaluator evaluator) throws QtfAssemblyException {
        return ExpressionAssembler.assemble(nodes, evaluator)
                // No dangling data in the stack
                .andThen(JvmFunction.insn(Opcodes.POP2));
    }
}
