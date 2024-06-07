package com.fiskmods.quantify.assembly;

import com.fiskmods.quantify.exception.QtfAssemblyException;
import com.fiskmods.quantify.insn.CstInsnNode;
import com.fiskmods.quantify.insn.InsnNode;
import com.fiskmods.quantify.insn.MemberInsnNode;
import com.fiskmods.quantify.member.QtfMemory;
import com.fiskmods.quantify.script.QtfEvaluator;

import java.util.List;
import java.util.function.DoubleBinaryOperator;

import static com.fiskmods.quantify.insn.Instruction.*;

@FunctionalInterface
public interface Assembler {
    AssemblyRunnable assemble(List<InsnNode> nodes, AssemblyRunnable.Supplier nextLine, QtfEvaluator evaluator) throws QtfAssemblyException;

    static Object negate(Object obj) {
        if (obj instanceof AssemblyFunction func) {
            return (AssemblyFunction) t -> -func.apply(t);
        }
        return -((Number) obj).doubleValue();
    }

    static Object wrap(DoubleBinaryOperator func, Object left, Object right) {
        if (left instanceof AssemblyFunction a) {
            if (right instanceof AssemblyFunction b) {
                // Var, var
                return (AssemblyFunction) t ->
                        func.applyAsDouble(a.apply(t), b.apply(t));
            }
            // Var, const
            double b1 = ((Number) right).doubleValue();
            return (AssemblyFunction) t -> func.applyAsDouble(a.apply(t), b1);
        }
        if (right instanceof AssemblyFunction b) {
            // Const, var
            double a = ((Number) left).doubleValue();
            return (AssemblyFunction) t -> func.applyAsDouble(a, b.apply(t));
        }
        // Const, const
        return func.applyAsDouble(
                ((Number) left).doubleValue(),
                ((Number) right).doubleValue());
    }

    static Object unwrap(Object obj) {
        if (obj instanceof AssemblyFunction || obj instanceof Number) {
            return obj;
        }
        if (obj instanceof InsnNode node) {
            if (isVariable(node.instruction) && obj instanceof MemberInsnNode member) {
                return QtfMemory.get(member.id, member.isNegative());
            }
            if (obj instanceof CstInsnNode cst) {
                return cst.value;
            }
            if (isConstant(node.instruction)) {
                return getConstantValue(node.instruction);
            }
        }
        return null;
    }

    static Object unwrapIfNecessary(Object obj) {
        if (obj instanceof InsnNode node) {
            return Assembler.unwrap(node);
        }
        return obj;
    }
}
