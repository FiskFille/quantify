package com.fiskmods.quantify.jvm;

import com.fiskmods.quantify.lexer.token.Operator;
import com.fiskmods.quantify.parser.element.Assignable;
import com.fiskmods.quantify.parser.element.NumLiteral;
import com.fiskmods.quantify.parser.element.Value;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.*;

public interface VarAddress extends Assignable {
    VariableType type();
    int id();
    boolean isNegated();

    default int localIndex() {
        return type().localIndex(id());
    }

    @Override
    default void apply(MethodVisitor mv) {
        if (type().isExternal()) {
            JvmUtil.arrayLoad(mv, type().refOffset(), id());
            return;
        }
        mv.visitVarInsn(DLOAD, localIndex());
    }

    @Override
    default void modify(MethodVisitor mv, Value value, Operator operator) {
        if (type().isExternal()) {
            JvmUtil.arrayModify(mv, type().refOffset(), id(), value.andThen(operator));
            return;
        }
        int index = localIndex();
        mv.visitVarInsn(DLOAD, index);
        value.apply(mv);
        operator.apply(mv);
        mv.visitVarInsn(DSTORE, index);
    }

    @Override
    default void set(MethodVisitor mv, Value value) {
        if (type().isExternal()) {
            JvmUtil.arrayStore(mv, type().refOffset(), id(), value);
            return;
        }
        value.apply(mv);
        mv.visitVarInsn(DSTORE, localIndex());
    }

    @Override
    default void lerp(MethodVisitor mv, Value value, Value progress, boolean rotational) {
        if (progress instanceof NumLiteral(double v)) {
            if (v == 0) {
                return;
            }
            if (v == 1) {
                set(mv, value);
                return;
            }
        }
        final String QTF_MATH = "com/fiskmods/quantify/library/QtfMath";

        // Interpolating towards 0 is the same as multiplying by (1-progress)
        if (!rotational && value instanceof NumLiteral(double v) && v == 0) {
            if (type().isExternal()) {
                JvmUtil.arrayModify(mv, type().refOffset(), id(), ignored -> {
                    mv.visitInsn(DCONST_1);
                    progress.apply(mv);
                    mv.visitInsn(DSUB);
                    mv.visitInsn(DMUL);
                });
            }
            else {
                int index = localIndex();
                mv.visitVarInsn(DLOAD, index);
                mv.visitInsn(DCONST_1);
                progress.apply(mv);
                mv.visitInsn(DSUB);
                mv.visitInsn(DMUL);
                mv.visitVarInsn(DSTORE, index);
            }
            return;
        }

        if (type().isExternal()) {
            JvmUtil.arrayModify(mv, type().refOffset(), id(), ignored -> {
                progress.apply(mv);
                value.apply(mv);
                JvmUtil.arrayLoad(mv, type().refOffset(), id());
                mv.visitInsn(DSUB);
                if (rotational) {
                    mv.visitMethodInsn(INVOKESTATIC, QTF_MATH, "wrapAngleToPi", "(D)D", false);
                }
                mv.visitInsn(DMUL);
                mv.visitInsn(DADD);
            });
        }
        else {
            int index = localIndex();
            mv.visitVarInsn(DLOAD, index);
            progress.apply(mv);
            value.apply(mv);
            mv.visitVarInsn(DLOAD, index);
            mv.visitInsn(DSUB);
            if (rotational) {
                mv.visitMethodInsn(INVOKESTATIC, QTF_MATH, "wrapAngleToPi", "(D)D", false);
            }
            mv.visitInsn(DMUL);
            mv.visitInsn(DADD);
            mv.visitVarInsn(DSTORE, index);
        }
    }

    static VarAddress create(VariableType type, int id, boolean isNegated) {
        return new Impl(type, id, isNegated);
    }

    record Impl(VariableType type, int id, boolean isNegated) implements VarAddress {
    }
}
