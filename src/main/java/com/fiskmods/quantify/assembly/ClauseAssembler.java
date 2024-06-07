package com.fiskmods.quantify.assembly;

import com.fiskmods.quantify.exception.QtfAssemblyException;
import com.fiskmods.quantify.insn.InsnNode;
import com.fiskmods.quantify.insn.WithInsnNode;
import com.fiskmods.quantify.member.QtfMemory;
import com.fiskmods.quantify.script.QtfEvaluator;

import java.util.List;

import static com.fiskmods.quantify.insn.Instruction.*;

public enum ClauseAssembler implements Assembler {
    INSTANCE;

    public static final AssemblerFactory FACTORY = line -> switch (line.get(0).instruction) {
        case IF, WTH, NSP, END -> INSTANCE;
        default -> null;
    };

    /* Empty AssemblyRunnable constant for logic purposes */
    private static final AssemblyRunnable BREAK = memory -> {};

    @Override
    public AssemblyRunnable assemble(List<InsnNode> nodes, AssemblyRunnable.Supplier nextLine, QtfEvaluator evaluator) throws QtfAssemblyException {
        InsnNode node = nodes.get(0);
        return switch (node.instruction) {
            case END -> BREAK;
            case IF -> assembleIf(node, nodes, nextLine, evaluator);
            case WTH -> assembleWith(node, nodes, nextLine, evaluator);
            case NSP -> AssemblyRunnable.EMPTY;
            default -> throw evaluator.error("invalid instruction", node);
        };
    }

    private AssemblyRunnable assembleIf(InsnNode node, List<InsnNode> nodes, AssemblyRunnable.Supplier nextLine,
                                        QtfEvaluator evaluator) throws QtfAssemblyException {
        Object result = ExpressionAssembler.assemble(nodes.subList(1, nodes.size()), evaluator);

        if (result instanceof Number n) {
            AssemblyRunnable r = assembleClauseLines(nextLine);
            return n.doubleValue() > 0 ? r : AssemblyRunnable.EMPTY;
        }

        if (result instanceof AssemblyFunction f) {
            AssemblyRunnable r = assembleClauseLines(nextLine);
            return memory -> {
                if (f.applyAsBoolean(memory)) {
                    r.run(memory);
                }
            };
        }

        throw evaluator.error("invalid result", node);
    }

    private AssemblyRunnable assembleWith(InsnNode node, List<InsnNode> nodes, AssemblyRunnable.Supplier nextLine,
                                          QtfEvaluator evaluator) throws QtfAssemblyException {
        Object result = ExpressionAssembler.assemble(nodes.subList(1, nodes.size()), evaluator);

        if (!(node instanceof WithInsnNode)) {
            throw evaluator.error("mismatched instruction type: " + node.getClass(), node);
        }
        AssemblyRunnable r = assembleClauseLines(nextLine);

        // Do nothing if lerp progress was 0
        if (result instanceof Number n && n.doubleValue() == 0) {
            return AssemblyRunnable.EMPTY;
        }
        return QtfMemory.set(((WithInsnNode) node).id, false, result)
                .andThen(r);
    }

    private AssemblyRunnable assembleClauseLines(AssemblyRunnable.Supplier nextLine) throws QtfAssemblyException {
        AssemblyRunnable runnable = AssemblyRunnable.EMPTY;
        AssemblyRunnable next;
        while ((next = nextLine.get()) != null) {
            if (next == BREAK) {
                break;
            }
            runnable = runnable.andThen(next);
        }
        return runnable;
    }
}
