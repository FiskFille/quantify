package com.fiskmods.quantify.assembly;

import com.fiskmods.quantify.exception.QtfAssemblyException;
import com.fiskmods.quantify.insn.InsnNode;
import com.fiskmods.quantify.insn.MemberInsnNode;
import com.fiskmods.quantify.member.QtfFunction;
import com.fiskmods.quantify.member.QtfMemory;
import com.fiskmods.quantify.script.QtfEvaluator;

import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleBinaryOperator;

import static com.fiskmods.quantify.insn.Instruction.*;

public class ExpressionAssembler {
    private static final int[] OP_ORDER;

    static {
        int[][] order = {
                { POW },
                { MUL, DIV, MOD },
                { ADD, SUB },
                { EQS, NEQ, LT, GT, LEQ, GEQ },
                { AND },
                { OR }
        };
        OP_ORDER = new int[order.length];
        for (int i = 0; i < order.length; ++i) {
            int bits = 0;
            for (int op : order[i]) {
                bits |= 1 << (op & MASK_VALUE);
            }
            OP_ORDER[i] = bits;
        }
    }

    public static Object assemble(List<InsnNode> nodes, QtfEvaluator evaluator) throws QtfAssemblyException {
        List<Object> assembly = new ArrayList<>(nodes);
        assembleBody(assembly, evaluator, null);
        return isolateAssembly(assembly);
    }

    private static Object isolateAssembly(List<Object> assembly) {
        // TODO: Make sure only one entry remains in assembly
        return Assembler.unwrapIfNecessary(assembly.get(0));
    }

    private static void assembleBody(List<Object> assembly, QtfEvaluator evaluator, MemberInsnNode funcNode)
            throws QtfAssemblyException {
        BracketMatcher brackets = new BracketMatcher();

        for (int i = 0; i < assembly.size(); ++i) {
            if (!(assembly.get(i) instanceof InsnNode node)) {
                continue;
            }
            if (node.instruction == FRUN && node instanceof MemberInsnNode m) {
                QtfFunction func = evaluator.getFunction(m);
                if (func.parameters() != 0) {
                    throw evaluator.error("incorrect number of parameters for function: 0, expected %s"
                            .formatted(func.parameters()), m);
                }
                assembly.set(i, QtfMemory.run(m.id, m.isNegative(), new Object[0]));
                continue;
            }

            i = brackets.match(i, node.instruction, (start, end) -> {
                List<Object> body = new ArrayList<>(assembly.subList(start + 1, end));
                if (start > 0 && assembly.get(start - 1) instanceof MemberInsnNode m && m.instruction == FREF) {
                    assembleBody(body, evaluator, m);
                    assembly.subList(start - 1, end + 1).clear();
                    assembly.addAll(start - 1, body);
                    return;
                }
                assembleBody(body, evaluator, null);
                assembly.subList(start, end + 1).clear();
                assembly.addAll(start, body);
            });
        }

        if (funcNode != null) {
            assembleFunction(assembly, evaluator, funcNode);
            return;
        }

        for (int ops : OP_ORDER) {
            assembleOperations(assembly, evaluator, ops);
        }
    }

    private static void assembleFunction(List<Object> assembly, QtfEvaluator evaluator, MemberInsnNode funcNode)
            throws QtfAssemblyException {
        List<Object> parameters = new ArrayList<>();

        for (int i = 0, startIndex = 0; i < assembly.size(); ++i) {
            // The last parameter has no NXT
            if (i == assembly.size() - 1) {
                ++i;
            }
            else if (!(assembly.get(i) instanceof InsnNode n && n.instruction == NXT)) {
                continue;
            }
            List<Object> body = new ArrayList<>(assembly.subList(startIndex, i));
            assembleBody(body, evaluator, null);
            parameters.add(isolateAssembly(body));
            startIndex = i + 1;
        }

        QtfFunction func = evaluator.getFunction(funcNode);
        if (func.parameters() != parameters.size()) {
            throw evaluator.error("incorrect number of parameters for function: %s, expected %s"
                    .formatted(parameters.size(), func.parameters()), funcNode);
        }
        assembly.clear();
        assembly.add(QtfMemory.run(funcNode.id, funcNode.isNegative(), parameters.toArray()));
    }

    private static void assembleOperations(List<Object> assembly, QtfEvaluator evaluator, int ops)
            throws QtfAssemblyException {
        for (int i = assembly.size() - 1; i >= 0; --i) {
            if (assembly.get(i) instanceof InsnNode node) {
                // Skip if non-operator, or if we're not looking for that operator right now
                if (!isOperator(node.instruction) || (ops & (1 << (node.instruction & MASK_VALUE))) == 0) {
                    continue;
                }
                assembleOperation(assembly, evaluator, node, i);
            }
        }
    }

    private static void assembleOperation(List<Object> assembly, QtfEvaluator evaluator, InsnNode node, int index)
            throws QtfAssemblyException {
        if (index <= 0 || index + 1 >= assembly.size()) {
            throw evaluator.error("invalid operator position", node);
        }
        Object left = Assembler.unwrap(assembly.get(index - 1));
        DoubleBinaryOperator func = getOperatorFunction(node.instruction);
        if (func == null) {
            throw evaluator.error("invalid operator", node);
        }
        if (left == null) {
            throw evaluator.error("invalid first term", node, -1);
        }

        Object right = assembly.get(index + 1);
        boolean rightNegated = false;
        if (index + 2 < assembly.size() && right instanceof InsnNode n && n.instruction == SUB) {
            right = Assembler.negate(Assembler.unwrap(assembly.get(index + 2)));
            rightNegated = true;
        }
        else {
            right = Assembler.unwrap(right);
        }
        if (right == null) {
            throw evaluator.error("invalid second term", node, 1);
        }

        if (rightNegated) {
            assembly.remove(index + 2);
        }
        assembly.remove(index + 1);
        assembly.remove(index);
        assembly.set(--index, Assembler.wrap(func, left, right));
    }
}
