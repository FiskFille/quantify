package com.fiskmods.quantify.jvm;

import com.fiskmods.quantify.lexer.token.Operator;
import com.fiskmods.quantify.parser.element.NumLiteral;
import com.fiskmods.quantify.parser.element.Value;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.*;

public class JvmUtil {
    public static void iconst(MethodVisitor mv, int i) {
        switch (i) {
            case 0 -> mv.visitInsn(ICONST_0);
            case 1 -> mv.visitInsn(ICONST_1);
            case 2 -> mv.visitInsn(ICONST_2);
            case 3 -> mv.visitInsn(ICONST_3);
            case 4 -> mv.visitInsn(ICONST_4);
            case 5 -> mv.visitInsn(ICONST_5);
            default -> mv.visitLdcInsn(i);
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

    public static void set(MethodVisitor mv, VarAddress<?>[] targets, Value value) {
        // Complex expressions only get calculated once for multi-var assignments
        if (targets.length > 1 && !(value instanceof NumLiteral)) {
            Value newValue = value;

            for (VarAddress<?> target : targets) {
                target.set(mv, newValue.negateIf(target.isNegated()));

                // For all targets after the first, set them to the first target
                if (newValue == value) {
                    newValue = ((Value) target::apply).negateIf(target.isNegated());
                }
            }
            return;
        }

        for (VarAddress<?> target : targets) {
            target.set(mv, value.negateIf(target.isNegated()));
        }
    }

    public static void modify(MethodVisitor mv, VarAddress<?>[] targets, Value value, Operator operator) {
        for (VarAddress<?> target : targets) {
            target.modify(mv, value.negateIf(target.isNegated()), operator);
        }
    }

    public static void lerp(MethodVisitor mv, VarAddress<?>[] targets, Value progress, boolean rotational, Value value) {
        if (progress instanceof NumLiteral(double v)) {
            if (v == 0) {
                return;
            }
            if (v == 1) {
                set(mv, targets, value);
                return;
            }
        }
        for (VarAddress<?> target : targets) {
            target.lerp(mv, value.negateIf(target.isNegated()), progress, rotational);
        }
    }
}
