package com.fiskmods.quantify.member;

import com.fiskmods.quantify.jvm.*;
import com.fiskmods.quantify.parser.element.NumLiteral;

import static org.objectweb.asm.Opcodes.*;

public class QtfMemory {
    public static final int LOCAL_INDEX = 3;

    private final double[] output;
    private final FunctionAddress[] functions;

    public QtfMemory(double[] output, FunctionAddress[] functions) {
        this.output = output;
        this.functions = functions;
    }

    public void run(JvmRunnable runnable, double[] input) {
        runnable.run(input, output);
    }

    public void print() {
        for (int i = 0; i < output.length; ++i) {
            System.out.println("V " + i + ": " + output[i]);
        }
        for (int i = 0; i < functions.length; ++i) {
            System.out.println("F " + i + ": " + functions[i]);
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
        return mv -> {
            for (Address a : addresses) {
                if (a.type.isExternal()) {
                    JvmUtil.arrayStore(mv, a.type.refOffset(), a.id, result.negateIf(a.isNegated));
                    continue;
                }
                result.negateIf(a.isNegated).apply(mv);
                mv.visitVarInsn(DSTORE, a.id * 2 + LOCAL_INDEX);
            }
        };
    }

    public static JvmFunction set(Address[] addresses, JvmFunction result, JvmFunction operator) {
        return mv -> {
            for (Address a : addresses) {
                if (a.type.isExternal()) {
                    JvmUtil.arrayModify(mv, a.type.refOffset(), a.id,
                            result.negateIf(a.isNegated).andThen(operator));
                    continue;
                }
                mv.visitVarInsn(DLOAD, a.id * 2 + LOCAL_INDEX);
                result.negateIf(a.isNegated).apply(mv);
                operator.apply(mv);
                mv.visitVarInsn(DSTORE, a.id * 2 + LOCAL_INDEX);
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

    public static JvmFunction lerp(Address var, JvmFunction lerp, boolean rotational, JvmFunction result) {
        final String QTF_MATH = "com/fiskmods/quantify/library/QtfMath";
        return mv -> {
            if (!rotational && result instanceof NumLiteral(double value) && value == 0) {
                if (var.type.isExternal()) {
                    JvmUtil.arrayModify(mv, var.type.refOffset(), var.id, ignored -> {
                        mv.visitInsn(DCONST_1);
                        lerp.apply(mv);
                        mv.visitInsn(DSUB);
                        mv.visitInsn(DMUL);
                    });
                }
                else {
                    mv.visitVarInsn(DLOAD, var.id * 2 + LOCAL_INDEX);
                    mv.visitInsn(DCONST_1);
                    lerp.apply(mv);
                    mv.visitInsn(DSUB);
                    mv.visitInsn(DMUL);
                    mv.visitVarInsn(DSTORE, var.id * 2 + LOCAL_INDEX);
                }
                return;
            }

            if (var.type.isExternal()) {
                JvmUtil.arrayModify(mv, var.type.refOffset(), var.id, ignored -> {
                    lerp.apply(mv);
                    result.apply(mv);
                    JvmUtil.arrayLoad(mv, var.type.refOffset(), var.id);
                    mv.visitInsn(DSUB);
                    if (rotational) {
                        mv.visitMethodInsn(INVOKESTATIC, QTF_MATH, "wrapAngleToPi", "(D)D", false);
                    }
                    mv.visitInsn(DMUL);
                    mv.visitInsn(DADD);
                });
            }
            else {
                mv.visitVarInsn(DLOAD, var.id * 2 + LOCAL_INDEX);
                lerp.apply(mv);
                result.apply(mv);
                mv.visitVarInsn(DLOAD, var.id * 2 + LOCAL_INDEX);
                mv.visitInsn(DSUB);
                if (rotational) {
                    mv.visitMethodInsn(INVOKESTATIC, QTF_MATH, "wrapAngleToPi", "(D)D", false);
                }
                mv.visitInsn(DMUL);
                mv.visitInsn(DADD);
                mv.visitVarInsn(DSTORE, var.id * 2 + LOCAL_INDEX);
            }
        };
    }

    public static JvmFunction lerp(Address[] addresses, JvmFunction lerp, boolean rotational, JvmFunction result) {
        return mv -> {
            for (Address a : addresses) {
                lerp(a, lerp, rotational, result.negateIf(a.isNegated)).apply(mv);
            }
        };
    }

    public record Address(int id, VariableType type, boolean isNegated) {
    }
}
