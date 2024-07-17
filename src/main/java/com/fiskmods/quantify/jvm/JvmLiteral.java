package com.fiskmods.quantify.jvm;

import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.DCONST_0;
import static org.objectweb.asm.Opcodes.DCONST_1;

public record JvmLiteral(double value, JvmFunction function) implements JvmFunction {
    @Override
    public void apply(MethodVisitor mv) {
        function.apply(mv);
    }

    public static JvmLiteral dconst(double value) {
        if (value == 0) {
            return new JvmLiteral(0, JvmFunction.insn(DCONST_0));
        }
        if (value == 1) {
            return new JvmLiteral(1, JvmFunction.insn(DCONST_1));
        }
        return new JvmLiteral(value, mv -> mv.visitLdcInsn(value));
    }
}
