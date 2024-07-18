package com.fiskmods.quantify.jvm;

import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.DCONST_0;
import static org.objectweb.asm.Opcodes.DCONST_1;

public record JvmLiteral(double value) implements JvmFunction {
    @Override
    public void apply(MethodVisitor mv) {
        if (value == 0) {
            mv.visitInsn(DCONST_0);
        }
        else if (value == 1) {
            mv.visitInsn(DCONST_1);
        }
        else {
            mv.visitLdcInsn(value);
        }
    }
}
