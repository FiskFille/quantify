package com.fiskmods.quantify.member;

import com.fiskmods.quantify.assembly.AssemblyFunction;
import com.fiskmods.quantify.assembly.AssemblyRunnable;
import com.fiskmods.quantify.exception.QtfExecutionException;
import com.fiskmods.quantify.library.QtfMath;

import java.util.Arrays;
import java.util.function.DoubleBinaryOperator;

public class QtfMemory {
    private final double[] vars;
    private final QtfFunction[] funcs;

    private final int[] inputIndexMap;

    public QtfMemory(double[] vars, QtfFunction[] funcs, int[] inputIndexMap) {
        this.vars = vars;
        this.funcs = funcs;
        this.inputIndexMap = inputIndexMap;
    }

    public void print() {
        for (int i = 0; i < vars.length; ++i) {
            System.out.println("V " + i + ": " + vars[i]);
        }
        for (int i = 0; i < funcs.length; ++i) {
            System.out.println("F " + i + ": " + funcs[i]);
        }
    }

    public void loadInputs(double[] args) {
        for (int i = 0; i < Math.min(args.length, inputIndexMap.length); ++i) {
            int id = inputIndexMap[i];
            if (id > -1) {
                vars[id] = args[i];
            }
        }
    }

    public VarReference resolve(int id) {
        if (id < 0 || id >= vars.length) {
            return VarReference.EMPTY;
        }
        return new VarReference() {
            @Override
            public double get() {
                return vars[id];
            }

            @Override
            public void set(double value) {
                vars[id] = value;
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

    public static AssemblyFunction get(int id, boolean negated) {
        return negated
                ? memory -> -memory.vars[id]
                : memory -> memory.vars[id];
    }

    public static AssemblyRunnable init(int id) {
        return memory -> memory.vars[id] = 0;
    }

    public static AssemblyRunnable set(int id, boolean negated, Object result) {
        return negated
                ? memory -> memory.vars[id] = -memory.cast(result)
                : memory -> memory.vars[id] = memory.cast(result);
    }

    public static AssemblyRunnable set(int id, boolean negated, Object result, DoubleBinaryOperator operator) {
        return negated
                ? memory -> memory.vars[id] = operator.applyAsDouble(memory.vars[id], -memory.cast(result))
                : memory -> memory.vars[id] = operator.applyAsDouble(memory.vars[id], memory.cast(result));
    }

    public static AssemblyRunnable set(int[] ids, boolean negated, Object result) {
        System.out.println(Arrays.toString(ids) + ", " + negated + ", " + result);
        return negated ? memory -> {
            double value = -memory.cast(result);
            for (int id : ids) {
                memory.vars[id] = value;
            }
        } : memory -> {
            double value = memory.cast(result);
            for (int id : ids) {
                memory.vars[id] = value;
            }
        };
    }

    public static AssemblyRunnable set(Address[] addresses, Object result) {
        return memory -> {
            double value = memory.cast(result);
            for (Address a : addresses) {
                memory.vars[a.id] = a.negated ? -value : value;
            }
        };
    }

    public static AssemblyRunnable set(int[] ids, boolean negated, Object result, DoubleBinaryOperator operator) {
        return negated ? memory -> {
            double value = -memory.cast(result);
            for (int id : ids) {
                memory.vars[id] = operator.applyAsDouble(memory.vars[id], value);
            }
        } : memory -> {
            double value = memory.cast(result);
            for (int id : ids) {
                memory.vars[id] = operator.applyAsDouble(memory.vars[id], value);
            }
        };
    }

    public static AssemblyRunnable set(Address[] addresses, Object result, DoubleBinaryOperator operator) {
        return memory -> {
            double value = memory.cast(result);
            for (Address a : addresses) {
                memory.vars[a.id] = operator.applyAsDouble(memory.vars[a.id],
                        a.negated ? -value : value);
            }
        };
    }

    public static AssemblyFunction run(int id, boolean negated, Object[] params) {
        return negated
                ? memory -> -memory.funcs[id].run(memory, params)
                : memory -> memory.funcs[id].run(memory, params);
    }

    public static AssemblyRunnable lerp(int varId, int lerpId, boolean negated, boolean rotational, Object result) {
        return rotational
                ? memory -> memory.vars[varId] = QtfMath.lerpRot(memory.vars[lerpId], memory.vars[varId], negated ? -memory.cast(result) : memory.cast(result))
                : memory -> memory.vars[varId] = QtfMath.lerp(memory.vars[lerpId], memory.vars[varId], negated ? -memory.cast(result) : memory.cast(result));
    }

    public static AssemblyRunnable lerp(int[] varIds, int lerpId, boolean negated, boolean rotational, Object result) {
        return rotational ? memory -> {
            double value = negated ? -memory.cast(result) : memory.cast(result);
            for (int id : varIds) {
                memory.vars[id] = QtfMath.lerp(memory.vars[lerpId], memory.vars[id], value);
            }
        } : memory -> {
            double value = negated ? -memory.cast(result) : memory.cast(result);
            for (int id : varIds) {
                memory.vars[id] = QtfMath.lerpRot(memory.vars[lerpId], memory.vars[id], value);
            }
        };
    }

    public static AssemblyRunnable lerp(Address[] addresses, int lerpId, boolean rotational, Object result) {
        return rotational ? memory -> {
            double value = memory.cast(result);
            for (Address a : addresses) {
                memory.vars[a.id] = QtfMath.lerp(memory.vars[lerpId], memory.vars[a.id],
                        a.negated ? -value : value);
            }
        } : memory -> {
            double value = memory.cast(result);
            for (Address a : addresses) {
                memory.vars[a.id] = QtfMath.lerpRot(memory.vars[lerpId], memory.vars[a.id],
                        a.negated ? -value : value);
            }
        };
    }

    public record Address(int id, boolean negated) {
    }
}
