package com.fiskmods.quantify.parser.element;

import com.fiskmods.quantify.jvm.JvmFunction;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import static org.objectweb.asm.Opcodes.DNEG;

public interface Value extends JvmFunction {
    Value ZERO = mv -> mv.visitInsn(Opcodes.DCONST_0);

    @Override
    default Value negate() {
        return new NegatedValue(this);
    }

    @Override
    default Value negateIf(boolean shouldNegate) {
        return (Value) JvmFunction.super.negateIf(shouldNegate);
    }

    record NegatedValue(Value val) implements Value {
        @Override
        public Value negate() {
            return val;
        }

        @Override
        public void apply(MethodVisitor mv) {
            val.apply(mv);
            mv.visitInsn(DNEG);
        }
    }
}
