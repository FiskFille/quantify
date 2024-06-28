package com.fiskmods.quantify.assembly;

import com.fiskmods.quantify.exception.QtfAssemblyException;
import com.fiskmods.quantify.insn.InsnNode;
import com.fiskmods.quantify.insn.WithInsnNode;
import com.fiskmods.quantify.jvm.JvmFunction;
import com.fiskmods.quantify.jvm.VariableType;
import com.fiskmods.quantify.member.QtfMemory;
import com.fiskmods.quantify.script.QtfEvaluator;
import org.objectweb.asm.Label;

import java.util.List;

import static com.fiskmods.quantify.insn.Instruction.*;
import static org.objectweb.asm.Opcodes.D2I;
import static org.objectweb.asm.Opcodes.IFLE;

public enum ClauseAssembler implements Assembler {
    INSTANCE;

    public static final AssemblerFactory FACTORY = line -> switch (line.get(0).instruction) {
        case IF, WTH, NSP, END -> INSTANCE;
        default -> null;
    };

    /* Empty JvmFunction constant for logic purposes */
    private static final JvmFunction BREAK = mv -> {};

    @Override
    public JvmFunction assemble(List<InsnNode> nodes, JvmFunction.Supplier nextLine, QtfEvaluator evaluator) throws QtfAssemblyException {
        InsnNode node = nodes.get(0);
        return switch (node.instruction) {
            case END -> BREAK;
            case IF -> assembleIf(nodes, nextLine, evaluator);
            case WTH -> assembleWith(node, nodes, nextLine, evaluator);
            case NSP -> JvmFunction.EMPTY;
            default -> throw evaluator.error("invalid instruction", node);
        };
    }

    private JvmFunction assembleIf(List<InsnNode> nodes, JvmFunction.Supplier nextLine,
                                   QtfEvaluator evaluator) throws QtfAssemblyException {
        JvmFunction condition = ExpressionAssembler.assemble(nodes.subList(1, nodes.size()), evaluator);
        JvmFunction rest = assembleClauseLines(nextLine);
        return mv -> {
            Label l = new Label();
            condition.apply(mv);
            mv.visitInsn(D2I);
            mv.visitJumpInsn(IFLE, l);
            rest.apply(mv);
            mv.visitLabel(l);
        };
    }

    private JvmFunction assembleWith(InsnNode node, List<InsnNode> nodes, JvmFunction.Supplier nextLine,
                                     QtfEvaluator evaluator) throws QtfAssemblyException {
        JvmFunction result = ExpressionAssembler.assemble(nodes.subList(1, nodes.size()), evaluator);
        if (!(node instanceof WithInsnNode)) {
            throw evaluator.error("mismatched instruction type: " + node.getClass(), node);
        }
        JvmFunction rest = assembleClauseLines(nextLine);
        return QtfMemory.set(((WithInsnNode) node).id, VariableType.LOCAL, result)
                .andThen(rest);
    }

    private JvmFunction assembleClauseLines(JvmFunction.Supplier nextLine) throws QtfAssemblyException {
        JvmFunction function = JvmFunction.EMPTY;
        JvmFunction next;
        while ((next = nextLine.get()) != null) {
            if (next == BREAK) {
                break;
            }
            function = function.andThen(next);
        }
        return function;
    }
}
