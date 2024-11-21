package com.fiskmods.quantify.lexer.token;

import com.fiskmods.quantify.jvm.JvmFunction;
import org.objectweb.asm.MethodVisitor;

import java.util.function.DoubleBinaryOperator;

public enum Operator implements DoubleBinaryOperator, JvmFunction {
    ADD(2, _ADD, Double::sum),
    SUB(2, _SUB, (l, r) -> l - r),
    MUL(1, _MUL, (l, r) -> l * r),
    DIV(1, _DIV, (l, r) -> l / r),
    POW(0, _POW, Math::pow),
    MOD(1, _REM, (l, r) -> l % r),

    EQ(3, _EQS, (l, r) -> l == r ? 1 : 0),
    NEQ(3, _NEQ, (l, r) -> l == r ? 0 : 1),
    LT(3, _LT, (l, r) -> l < r ? 1 : 0),
    GT(3, _GT, (l, r) -> l > r ? 1 : 0),
    LEQ(3, _LEQ, (l, r) -> l <= r ? 1 : 0),
    GEQ(3, _GEQ, (l, r) -> l >= r ? 1 : 0),

    AND(4, _MUL, MUL),
    OR(5, _ADD, ADD),

    LERP(-1, null, null),
    LERP_ROT(-1, null, null);

    private final JvmFunction jvmFunction;
    private final DoubleBinaryOperator binaryOperator;
    private final int priority;

    Operator(int priority, JvmFunction jvmFunction, DoubleBinaryOperator binaryOperator) {
        this.jvmFunction = jvmFunction;
        this.binaryOperator = binaryOperator;
        this.priority = priority;
    }

    public int priority() {
        return priority;
    }

    @Override
    public double applyAsDouble(double left, double right) {
        if (binaryOperator == null) {
            throw new UnsupportedOperationException();
        }
        return binaryOperator.applyAsDouble(left, right);
    }

    @Override
    public void apply(MethodVisitor mv) {
        if (jvmFunction == null) {
            throw new UnsupportedOperationException();
        }
        jvmFunction.apply(mv);
    }
}
