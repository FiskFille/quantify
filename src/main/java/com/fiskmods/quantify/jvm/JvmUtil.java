package com.fiskmods.quantify.jvm;

import com.fiskmods.quantify.insn.CstInsnNode;
import com.fiskmods.quantify.insn.InsnNode;
import com.fiskmods.quantify.insn.MemberInsnNode;
import com.fiskmods.quantify.member.QtfMemory;
import org.objectweb.asm.MethodVisitor;

import static com.fiskmods.quantify.insn.Instruction.*;
import static org.objectweb.asm.Opcodes.*;

public class JvmUtil {
    public static JvmFunction dconst(double value) {
        if (value == 0) {
            return JvmFunction.insn(DCONST_0);
        }
        if (value == 1) {
            return JvmFunction.insn(DCONST_1);
        }
        return mv -> mv.visitLdcInsn(value);
    }

    public static JvmFunction binaryOperator(JvmFunction operator, JvmFunction left, JvmFunction right) {
        return mv -> {
            left.apply(mv);
            right.apply(mv);
            operator.apply(mv);
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
            return dconst(n.doubleValue());
        }
        if (obj instanceof InsnNode node) {
            if (isVariable(node.instruction) && obj instanceof MemberInsnNode member) {
                return QtfMemory.get(member.id, VariableType.get(member.instruction)).negateIf(member.isNegative());
            }
            if (obj instanceof CstInsnNode cst) {
                return dconst(cst.value);
            }
            if (isConstant(node.instruction)) {
                return dconst(getConstantValue(node.instruction));
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
