package com.fiskmods.quantify.assembly;

import com.fiskmods.quantify.exception.QtfAssemblyException;
import com.fiskmods.quantify.insn.InsnNode;
import com.fiskmods.quantify.insn.LerpInsnNode;
import com.fiskmods.quantify.insn.MemberInsnNode;
import com.fiskmods.quantify.member.QtfMemory;
import com.fiskmods.quantify.script.QtfEvaluator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.DoubleBinaryOperator;

import static com.fiskmods.quantify.insn.Instruction.*;

public enum AssignmentAssembler implements Assembler {
    INSTANCE;

    public static final AssemblerFactory FACTORY = AssemblerFactory.singleton(INSTANCE);

    @Override
    public AssemblyRunnable assemble(List<InsnNode> nodes, AssemblyRunnable.Supplier nextLine, QtfEvaluator evaluator) throws QtfAssemblyException {
        List<MemberInsnNode> vars = new ArrayList<>();
        Iterator<InsnNode> iter = nodes.iterator();
        InsnNode node, assignment = null;
        int offset = 0;
        int bits = 0;

        while (iter.hasNext()) {
            node = iter.next();
            if (!(node instanceof MemberInsnNode var)) {
                throw evaluator.error("not a var node", node);
            }
            if (!iter.hasNext()) {
                // Definitions don't need assignments
                if (var.instruction != DEF) {
                    throw evaluator.error("missing assignment", var);
                }
                return QtfMemory.init(var.id);
            }

            node = iter.next();
            if (node.instruction == NXT) {
                offset += 2;
                bits |= var.isNegative() ? 2 : 1;
                vars.add(var);
                continue;
            }
            if (!isAssignment(node.instruction)) {
                throw evaluator.error("invalid assignment", node);
            }

            // Assemble right away if there are no other vars
            if (vars.isEmpty()) {
                return assembleVar(nodes.subList(offset + 2, nodes.size()), var, node, evaluator);
            }
            assignment = node;
            bits |= var.isNegative() ? 2 : 1;
            vars.add(var);
            break;
        }

        assert assignment != null;
        return assembleVars(nodes.subList(offset + 2, nodes.size()), vars, assignment, bits, evaluator);
    }

    private AssemblyRunnable assembleVar(List<InsnNode> nodes, MemberInsnNode var, InsnNode assignment,
                                         QtfEvaluator evaluator) throws QtfAssemblyException {
        Object result = ExpressionAssembler.assemble(nodes, evaluator);
        return switch (assignment.instruction) {
            case EQ -> QtfMemory.set(var.id, var.isNegative(), result);
            case LRP, RLRP -> {
                if (!(assignment instanceof LerpInsnNode lerp)) {
                    throw evaluator.error("mismatched instruction type: " + assignment.getClass(), assignment);
                }
                yield QtfMemory.lerp(var.id, lerp.id, var.isNegative(), assignment.instruction == RLRP, result);
            }
            default -> {
                DoubleBinaryOperator func = getOperatorFunction(toOperator(assignment.instruction));
                if (func == null) {
                    throw evaluator.error("invalid operator", assignment);
                }
                yield QtfMemory.set(var.id, var.isNegative(), result, func);
            }
        };
    }

    private AssemblyRunnable assembleVars(List<InsnNode> nodes, List<MemberInsnNode> vars, InsnNode assignment,
                                          int bits, QtfEvaluator evaluator) throws QtfAssemblyException {
        Object result = ExpressionAssembler.assemble(nodes, evaluator);

        if (assignment.instruction == LRP || assignment.instruction == RLRP) {
            if (!(assignment instanceof LerpInsnNode lerp)) {
                throw evaluator.error("mismatched instruction type: " + assignment.getClass(), assignment);
            }
            // Mix of negative and non-negative
            if (bits == 3) {
                return QtfMemory.lerp(collect(vars), lerp.id, assignment.instruction == RLRP, result);
            }
            return QtfMemory.lerp(vars.stream().mapToInt(t -> t.id).toArray(),
                    lerp.id, bits == 2, assignment.instruction == RLRP, result);
        }

        DoubleBinaryOperator func = null;
        if (assignment.instruction != EQ && (func = getOperatorFunction(toOperator(assignment.instruction))) == null) {
            throw evaluator.error("invalid operator", assignment);
        }

        // Mix of negative and non-negative
        if (bits == 3) {
            QtfMemory.Address[] addresses = collect(vars);
            if (func == null) {
                return QtfMemory.set(addresses, result);
            }
            return QtfMemory.set(addresses, result, func);
        }

        int[] ids = vars.stream().mapToInt(t -> t.id).toArray();
        if (func == null) {
            return QtfMemory.set(ids, bits == 2, result);
        }
        return QtfMemory.set(ids, bits == 2, result, func);
    }

    private QtfMemory.Address[] collect(List<MemberInsnNode> vars) {
        return vars.stream()
                .map(MemberInsnNode::toAddress)
                .toArray(QtfMemory.Address[]::new);
    }
}
