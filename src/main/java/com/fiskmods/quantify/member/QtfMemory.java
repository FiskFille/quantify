package com.fiskmods.quantify.member;

import com.fiskmods.quantify.jvm.*;
import com.fiskmods.quantify.parser.element.NumLiteral;

import static org.objectweb.asm.Opcodes.*;

public class QtfMemory {
    public static final int LOCAL_INDEX = 3;

    private final double[] output;

    public QtfMemory(double[] output) {
        this.output = output;
    }

    public void run(JvmRunnable runnable, double[] input) {
        runnable.run(input, output);
    }

    public void print() {
        for (int i = 0; i < output.length; ++i) {
            System.out.println("V " + i + ": " + output[i]);
        }
    }

    public VarReference resolve(int id) {
        if (id < 0 || id >= output.length) {
            return VarReference.EMPTY;
        }
        return new VarReference() {
            @Override
            public double get() {
                return output[id];
            }

            @Override
            public void set(double value) {
                output[id] = value;
            }
        };
    }

    public static JvmFunction get(int id, VariableType type) {
        return mv -> {
            if (type.isExternal()) {
                JvmUtil.arrayLoad(mv, type.refOffset(), id);
                return;
            }
            mv.visitVarInsn(DLOAD, id * 2 + LOCAL_INDEX);
        };
    }

    public static JvmFunction init(int id) {
        return mv -> {
            mv.visitInsn(DCONST_0);
            mv.visitVarInsn(DSTORE, id * 2 + LOCAL_INDEX);
        };
    }

    public static JvmFunction set(int id, VariableType type, JvmFunction result) {
        return mv -> {
            if (type.isExternal()) {
                JvmUtil.arrayStore(mv, type.refOffset(), id, result);
                return;
            }
            result.apply(mv);
            mv.visitVarInsn(DSTORE, id * 2 + LOCAL_INDEX);
        };
    }

    public static JvmFunction set(int id, VariableType type, JvmFunction result, JvmFunction operator) {
        return mv -> {
            if (type.isExternal()) {
                JvmUtil.arrayModify(mv, type.refOffset(), id, result.andThen(operator));
                return;
            }
            mv.visitVarInsn(DLOAD, id * 2 + LOCAL_INDEX);
            result.apply(mv);
            operator.apply(mv);
            mv.visitVarInsn(DSTORE, id * 2 + LOCAL_INDEX);
        };
    }

    public static JvmFunction set(Address[] addresses, JvmFunction result) {
        // Complex expressions only get calculated once for multi-var assignments
        if (addresses.length > 1 && !(result instanceof NumLiteral)) {
            return mv -> {
                JvmFunction value = result;

                for (Address a : addresses) {
                    set(a.id, a.type, value.negateIf(a.isNegated)).apply(mv);
                    if (value == result) {
                        value = get(a.id, a.type).negateIf(a.isNegated);
                    }
                }
            };
        }
        return mv -> {
            for (Address a : addresses) {
                set(a.id, a.type, result.negateIf(a.isNegated)).apply(mv);
            }
        };
    }

    public static JvmFunction set(Address[] addresses, JvmFunction result, JvmFunction operator) {
        return mv -> {
            for (Address a : addresses) {
                set(a.id, a.type, result.negateIf(a.isNegated), operator).apply(mv);
            }
        };
    }

    public static <T extends JvmFunction> JvmFunction run(FunctionAddress address, T[] params) {
        return mv -> {
            for (JvmFunction function : params) {
                function.apply(mv);
            }
            mv.visitMethodInsn(INVOKESTATIC, address.owner, address.name, address.descriptor, false);
        };
    }

    public static JvmFunction lerp(int id, VariableType type, JvmFunction progress, boolean rotational, JvmFunction result) {
        if (progress instanceof NumLiteral(double value)) {
            if (value == 0) {
                return JvmFunction.DO_NOTHING;
            }
            if (value == 1) {
                return set(id, type, result);
            }
        }
        final String QTF_MATH = "com/fiskmods/quantify/library/QtfMath";
        return mv -> {
            if (!rotational && result instanceof NumLiteral(double value) && value == 0) {
                if (type.isExternal()) {
                    JvmUtil.arrayModify(mv, type.refOffset(), id, ignored -> {
                        mv.visitInsn(DCONST_1);
                        progress.apply(mv);
                        mv.visitInsn(DSUB);
                        mv.visitInsn(DMUL);
                    });
                }
                else {
                    mv.visitVarInsn(DLOAD, id * 2 + LOCAL_INDEX);
                    mv.visitInsn(DCONST_1);
                    progress.apply(mv);
                    mv.visitInsn(DSUB);
                    mv.visitInsn(DMUL);
                    mv.visitVarInsn(DSTORE, id * 2 + LOCAL_INDEX);
                }
                return;
            }

            if (type.isExternal()) {
                JvmUtil.arrayModify(mv, type.refOffset(), id, ignored -> {
                    progress.apply(mv);
                    result.apply(mv);
                    JvmUtil.arrayLoad(mv, type.refOffset(), id);
                    mv.visitInsn(DSUB);
                    if (rotational) {
                        mv.visitMethodInsn(INVOKESTATIC, QTF_MATH, "wrapAngleToPi", "(D)D", false);
                    }
                    mv.visitInsn(DMUL);
                    mv.visitInsn(DADD);
                });
            }
            else {
                mv.visitVarInsn(DLOAD, id * 2 + LOCAL_INDEX);
                progress.apply(mv);
                result.apply(mv);
                mv.visitVarInsn(DLOAD, id * 2 + LOCAL_INDEX);
                mv.visitInsn(DSUB);
                if (rotational) {
                    mv.visitMethodInsn(INVOKESTATIC, QTF_MATH, "wrapAngleToPi", "(D)D", false);
                }
                mv.visitInsn(DMUL);
                mv.visitInsn(DADD);
                mv.visitVarInsn(DSTORE, id * 2 + LOCAL_INDEX);
            }
        };
    }

    public static JvmFunction lerp(Address[] addresses, JvmFunction progress, boolean rotational, JvmFunction result) {
        if (progress instanceof NumLiteral(double value)) {
            if (value == 0) {
                return JvmFunction.DO_NOTHING;
            }
            if (value == 1) {
                return set(addresses, result);
            }
        }
        return mv -> {
            for (Address a : addresses) {
                lerp(a.id, a.type, progress, rotational, result.negateIf(a.isNegated)).apply(mv);
            }
        };
    }

    public record Address(int id, VariableType type, boolean isNegated) {
    }
}
