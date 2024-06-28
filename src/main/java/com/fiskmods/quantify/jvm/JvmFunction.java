package com.fiskmods.quantify.jvm;

import com.fiskmods.quantify.exception.QtfAssemblyException;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import static com.fiskmods.quantify.insn.Instruction.*;
import static org.objectweb.asm.Opcodes.*;

@FunctionalInterface
public interface JvmFunction {
    JvmFunction EMPTY = new JvmFunction() {
        @Override
        public void apply(MethodVisitor mv) {
        }

        @Override
        public JvmFunction andThen(JvmFunction next) {
            return next;
        }
    };

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

    static JvmFunction getOperatorFunction(int operator) {
        return switch (operator) {
            case ADD, OR -> _ADD;
            case SUB -> _SUB;
            case MUL, AND -> _MUL;
            case DIV -> _DIV;
            case POW -> _POW;
            case MOD -> _REM;

            case EQS -> _EQS;
            case NEQ -> _NEQ;
            case LT  -> _LT;
            case GT  -> _GT;
            case LEQ -> _LEQ;
            case GEQ -> _GEQ;
            default -> null;
        };
    }

    @FunctionalInterface
    interface Supplier {
        JvmFunction get() throws QtfAssemblyException;
    }
}
