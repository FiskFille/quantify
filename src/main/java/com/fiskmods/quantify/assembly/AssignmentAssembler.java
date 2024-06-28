package com.fiskmods.quantify.assembly;

import com.fiskmods.quantify.exception.QtfAssemblyException;
import com.fiskmods.quantify.insn.InsnNode;
import com.fiskmods.quantify.insn.LerpInsnNode;
import com.fiskmods.quantify.insn.MemberInsnNode;
import com.fiskmods.quantify.jvm.JvmFunction;
import com.fiskmods.quantify.jvm.VariableType;
import com.fiskmods.quantify.member.QtfMemory;
import com.fiskmods.quantify.script.QtfEvaluator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.fiskmods.quantify.insn.Instruction.*;

public enum AssignmentAssembler implements Assembler {
    INSTANCE;

    public static final AssemblerFactory FACTORY = AssemblerFactory.singleton(INSTANCE);

    @Override
    public JvmFunction assemble(List<InsnNode> nodes, JvmFunction.Supplier nextLine, QtfEvaluator evaluator) throws QtfAssemblyException {
        List<MemberInsnNode> vars = new ArrayList<>();
        Iterator<InsnNode> iter = nodes.iterator();
        InsnNode node, assignment = null;
        int offset = 0;

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
                if (vars.isEmpty()) {
                    return QtfMemory.init(var.id);
                }

                // Multi-var definition needs init on all vars
                JvmFunction function = JvmFunction.EMPTY;
                for (MemberInsnNode m : vars) {
                    function = function.andThen(QtfMemory.init(m.id));
                }
                return function.andThen(QtfMemory.init(var.id));
            }

            node = iter.next();
            if (node.instruction == NXT) {
                offset += 2;
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
            vars.add(var);
            break;
        }

        assert assignment != null;
        return assembleVars(nodes.subList(offset + 2, nodes.size()), vars, assignment, evaluator);
    }

    private JvmFunction assembleVar(List<InsnNode> nodes, MemberInsnNode var, InsnNode assignment,
                                    QtfEvaluator evaluator) throws QtfAssemblyException {
        JvmFunction result = ExpressionAssembler.assemble(nodes, evaluator);
        return switch (assignment.instruction) {
            case EQ -> QtfMemory.set(var.id, VariableType.get(var.instruction), result.negateIf(var.isNegative()));
            case LRP, RLRP -> {
                if (!(assignment instanceof LerpInsnNode lerp)) {
                    throw evaluator.error("mismatched instruction type: " + assignment.getClass(), assignment);
                }
                yield QtfMemory.lerp(var.toAddress(), lerp.toAddress(),
                        assignment.instruction == RLRP, result.negateIf(var.isNegative()));
            }
            default -> {
                JvmFunction func = JvmFunction.getOperatorFunction(toOperator(assignment.instruction));
                if (func == null) {
                    throw evaluator.error("invalid operator", assignment);
                }
                yield QtfMemory.set(var.id, VariableType.get(var.instruction), result.negateIf(var.isNegative()), func);
            }
        };
    }

    private JvmFunction assembleVars(List<InsnNode> nodes, List<MemberInsnNode> vars,
                                     InsnNode assignment, QtfEvaluator evaluator) throws QtfAssemblyException {
        JvmFunction result = ExpressionAssembler.assemble(nodes, evaluator);

        if (assignment.instruction == LRP || assignment.instruction == RLRP) {
            if (!(assignment instanceof LerpInsnNode lerp)) {
                throw evaluator.error("mismatched instruction type: " + assignment.getClass(), assignment);
            }
            return QtfMemory.lerp(collect(vars), lerp.toAddress(), assignment.instruction == RLRP, result);
        }

        JvmFunction func = null;
        if (assignment.instruction != EQ && (func = JvmFunction.getOperatorFunction(toOperator(assignment.instruction))) == null) {
            throw evaluator.error("invalid operator", assignment);
        }

        QtfMemory.Address[] addresses = collect(vars);
        if (func == null) {
            return QtfMemory.set(addresses, result);
        }
        return QtfMemory.set(addresses, result, func);
    }

    private QtfMemory.Address[] collect(List<MemberInsnNode> vars) {
        return vars.stream()
                .map(MemberInsnNode::toAddress)
                .toArray(QtfMemory.Address[]::new);
    }
}
