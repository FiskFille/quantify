package com.fiskmods.quantify.jvm;

import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.*;

public class JvmUtil {
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
