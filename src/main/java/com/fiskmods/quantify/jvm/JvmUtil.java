package com.fiskmods.quantify.jvm;

import com.fiskmods.quantify.insn.CstInsnNode;
import com.fiskmods.quantify.insn.InsnNode;
import com.fiskmods.quantify.insn.MemberInsnNode;
import com.fiskmods.quantify.member.QtfMemory;
import org.objectweb.asm.MethodVisitor;

import static com.fiskmods.quantify.insn.Instruction.*;
import static org.objectweb.asm.Opcodes.*;

public class JvmUtil {
    public static JvmFunction binaryOperator(InsnNode node, JvmFunction operator, JvmFunction left, JvmFunction right) {
        if (node.instruction == MUL || node.instruction == AND) {
            // Any multiplication where one factor is 1 is redundant
            if (left instanceof JvmLiteral lit && lit.value() == 1) {
                return right;
            }
            if (right instanceof JvmLiteral lit && lit.value() == 1) {
                return left;
            }
        }
        if (node.instruction == DIV && right instanceof JvmLiteral lit && lit.value() == 1) {
            // Any division where the divisor is 1 is redundant
            return left;
        }
        if (left instanceof JvmLiteral l && right instanceof JvmLiteral r) {
            Double d = computeOperator(node.instruction, l.value(), r.value());
            if (d != null) {
                // Pre-compute literal arithmetic
                return new JvmLiteral(d);
            }
        }
        return mv -> {
            left.apply(mv);
            right.apply(mv);
            operator.apply(mv);
        };
    }

    private static Double computeOperator(int instruction, double l, double r) {
        return switch (instruction) {
            case ADD, OR -> l + r;
            case SUB -> l - r;
            case MUL, AND -> l * r;
            case DIV -> l / r;
            case POW -> Math.pow(l, r);
            case MOD -> l % r;

            case EQS -> l == r ? 1.0 : 0.0;
            case NEQ -> l == r ? 0.0 : 1.0;
            case LT  -> l < r ? 1.0 : 0.0;
            case GT  -> l > r ? 1.0 : 0.0;
            case LEQ -> l <= r ? 1.0 : 0.0;
            case GEQ -> l >= r ? 1.0 : 0.0;
            default -> null;
        };
    }

    public static void iconst(MethodVisitor mv, int index) {
        switch (index) {
            case 0 -> mv.visitInsn(ICONST_0);
            case 1 -> mv.visitInsn(ICONST_1);
            case 2 -> mv.visitInsn(ICONST_2);
            case 3 -> mv.visitInsn(ICONST_3);
            case 4 -> mv.visitInsn(ICONST_4);
            case 5 -> mv.visitInsn(ICONST_5);
            default -> mv.visitLdcInsn(index);
        };
    }

    public static JvmFunction toJvmIfNecessary(Object obj) {
        if (obj instanceof InsnNode node) {
            return toJvm(node);
        }
        return (JvmFunction) obj;
    }

    public static JvmFunction toJvm(Object obj) {
        if (obj instanceof JvmFunction f) {
            return f;
        }
        if (obj instanceof Number n) {
            return new JvmLiteral(n.doubleValue());
        }
        if (obj instanceof InsnNode node) {
            if (isVariable(node.instruction) && obj instanceof MemberInsnNode member) {
                return QtfMemory.get(member.id, VariableType.get(member.instruction)).negateIf(member.isNegative());
            }
            if (obj instanceof CstInsnNode cst) {
                return new JvmLiteral(cst.value);
            }
            if (isConstant(node.instruction)) {
                return new JvmLiteral(getConstantValue(node.instruction));
            }
        }
        return null;
    }

    public static void arrayLoad(MethodVisitor mv, int index, int arrayIndex) {
        mv.visitVarInsn(ALOAD, index);
        JvmUtil.iconst(mv, arrayIndex);
        mv.visitInsn(DALOAD);
    }

    public static void arrayStore(MethodVisitor mv, int index, int arrayIndex, JvmFunction result) {
        mv.visitVarInsn(ALOAD, index);
        JvmUtil.iconst(mv, arrayIndex);
        result.apply(mv);
        mv.visitInsn(DASTORE);
    }

    public static void arrayModify(MethodVisitor mv, int index, int arrayIndex, JvmFunction result) {
        mv.visitVarInsn(ALOAD, index);
        JvmUtil.iconst(mv, arrayIndex);
        mv.visitInsn(DUP2);
        mv.visitInsn(DALOAD);
        result.apply(mv);
        mv.visitInsn(DASTORE);
    }
}
