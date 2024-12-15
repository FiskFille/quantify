package com.fiskmods.quantify.parser.element;

import com.fiskmods.quantify.lexer.token.Operator;
import org.objectweb.asm.MethodVisitor;

record Operation(Value left, Value right, Operator op) implements Value {
    @Override
    public void apply(MethodVisitor mv) {
        left.apply(mv);
        right.apply(mv);
        op.apply(mv);
    }

    static Value wrap(Value left, Value right, Operator op) {
        switch (op) {
            case MUL, AND -> {
                // Any multiplication where one term is 0 or 1 is redundant
                if (left instanceof NumLiteral(double value)) {
                    if (value == 0) {
                        return left;
                    }
                    if (value == 1) {
                        return right;
                    }
                }
                if (right instanceof NumLiteral(double value)) {
                    if (value == 0) {
                        return right;
                    }
                    if (value == 1) {
                        return left;
                    }
                }
            }
            case DIV -> {
                if (left instanceof NumLiteral(double value) && value == 0) {
                    // Any division where the dividend is 0 is redundant
                    return left;
                }
                if (right instanceof NumLiteral(double value) && value == 1) {
                    // Any division where the divisor is 1 is redundant
                    return left;
                }
            }
        }
        if (left instanceof NumLiteral(double l) && right instanceof NumLiteral(double r)) {
            // Pre-compute literal arithmetic
            return new NumLiteral(op.applyAsDouble(l, r));
        }
        return new Operation(left, right, op);
    }
}
