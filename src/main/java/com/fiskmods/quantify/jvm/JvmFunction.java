package com.fiskmods.quantify.jvm;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.*;

@FunctionalInterface
public interface JvmFunction {
    JvmFunction DO_NOTHING = mv -> { };

    JvmFunction _ADD = insn(DADD);
    JvmFunction _SUB = insn(DSUB);
    JvmFunction _MUL = insn(DMUL);
    JvmFunction _DIV = insn(DDIV);
    JvmFunction _REM = insn(DREM);
    JvmFunction _POW = mv -> mv.visitMethodInsn(INVOKESTATIC,
            "java/lang/Math",
            "pow", "(DD)D", false);

    JvmFunction _EQS = comparator(IFEQ);
    JvmFunction _NEQ = comparator(IFNE);
    JvmFunction _LT = comparator(IFLT);
    JvmFunction _LEQ = comparator(IFLE);
    JvmFunction _GT = comparator(IFGT);
    JvmFunction _GEQ = comparator(IFGE);

    void apply(MethodVisitor mv);

    default JvmFunction andThen(JvmFunction next) {
        return mv -> {
            apply(mv);
            next.apply(mv);
        };
    }

    default JvmFunction negate() {
        return andThen(mv -> mv.visitInsn(DNEG));
    }

    default JvmFunction negateIf(boolean shouldNegate) {
        return shouldNegate ? negate() : this;
    }

    static JvmFunction insn(int opcode) {
        return mv -> mv.visitInsn(opcode);
    }

    static JvmFunction comparator(int opcode) {
        return mv -> {
            Label l = new Label();
            Label end = new Label();
            mv.visitInsn(DCMPG);
            mv.visitJumpInsn(opcode, l);
            mv.visitInsn(DCONST_0);
            mv.visitJumpInsn(GOTO, end);
            mv.visitLabel(l);
            mv.visitInsn(DCONST_1);
            mv.visitLabel(end);
        };
    }
}
