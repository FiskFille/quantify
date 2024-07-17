package com.fiskmods.quantify.member;

import com.fiskmods.quantify.assembly.AssemblyFunction;
import com.fiskmods.quantify.exception.QtfExecutionException;
import com.fiskmods.quantify.jvm.*;

import static org.objectweb.asm.Opcodes.*;

public class QtfMemory {
    public static final int LOCAL_INDEX = 3;

    private final double[] output;
    private final FunctionAddress[] funcs;

    public QtfMemory(double[] output, FunctionAddress[] funcs) {
        this.output = output;
        this.funcs = funcs;
    }

    public void run(JvmRunnable runnable, double[] input) {
        runnable.run(input, output);
    }

    public void print() {
        for (int i = 0; i < output.length; ++i) {
            System.out.println("V " + i + ": " + output[i]);
        }
        for (int i = 0; i < funcs.length; ++i) {
            System.out.println("F " + i + ": " + funcs[i]);
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

    double cast(Object result) throws QtfExecutionException {
        if (result instanceof AssemblyFunction f) {
            return f.apply(this);
        }
        try {
            return ((Number) result).doubleValue();
        }
        catch (ClassCastException | NullPointerException e) {
            throw new QtfExecutionException("Unknown result type: " + result);
        }
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
                mv.visitVarInsn(DSTORE, LOCAL_INDEX + a.id);
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

    public static JvmFunction run(FunctionAddress address, JvmFunction[] params) {
        return mv -> {
            for (JvmFunction function : params) {
                function.apply(mv);
            }
            mv.visitMethodInsn(INVOKESTATIC, address.owner, address.name, address.descriptor, false);
        };
    }

    public static JvmFunction lerp(Address var, Address lerp, boolean rotational, JvmFunction result) {
        final String QTF_MATH = "com/fiskmods/quantify/library/QtfMath";
        return mv -> {
            if (!rotational && result instanceof JvmLiteral lit && lit.value() == 0) {
                if (var.type.isExternal()) {
                    JvmUtil.arrayModify(mv, var.type.refOffset(), var.id, ignored -> {
                        mv.visitInsn(DCONST_1);
                        get(lerp.id, lerp.type).apply(mv);
                        mv.visitInsn(DSUB);
                        mv.visitInsn(DMUL);
                    });
                }
                else {
                    mv.visitVarInsn(DLOAD, var.id * 2 + LOCAL_INDEX);
                    mv.visitInsn(DCONST_1);
                    get(lerp.id, lerp.type).apply(mv);
                    mv.visitInsn(DSUB);
                    mv.visitInsn(DMUL);
                    mv.visitVarInsn(DSTORE, var.id * 2 + LOCAL_INDEX);
                }
                return;
            }

            if (var.type.isExternal()) {
                JvmUtil.arrayModify(mv, var.type.refOffset(), var.id, ignored -> {
                    get(lerp.id, lerp.type).apply(mv);
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
                get(lerp.id, lerp.type).apply(mv);
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

    public static JvmFunction lerp(Address[] addresses, Address lerp, boolean rotational, JvmFunction result) {
        return mv -> {
            for (Address a : addresses) {
                lerp(a, lerp, rotational, result.negateIf(a.isNegated)).apply(mv);
            }
        };
    }

    public record Address(int id, VariableType type, boolean isNegated) {
    }
}
