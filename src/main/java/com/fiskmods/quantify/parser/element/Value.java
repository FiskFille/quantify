package com.fiskmods.quantify.parser.element;

import com.fiskmods.quantify.parser.SyntaxElement;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.DNEG;

public interface Value extends SyntaxElement {
    @Override
    default Value negate() {
        return new NegatedValue(this);
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
