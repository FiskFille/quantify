package com.fiskmods.quantify.member;

import com.fiskmods.quantify.exception.QtfExecutionException;
import com.fiskmods.quantify.util.DoubleTernaryOperator;

import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleSupplier;
import java.util.function.DoubleUnaryOperator;

public record QtfFunction(int parameters, Func func) {
    public double run(QtfMemory memory, Object[] params) throws QtfExecutionException {
        return func.apply(i -> memory.cast(params[i]));
    }

    public static QtfFunction compose(DoubleSupplier func) {
        return new QtfFunction(0, p -> func.getAsDouble());
    }

    public static QtfFunction compose(DoubleUnaryOperator func) {
        return new QtfFunction(1, p -> func.applyAsDouble(p.get(0)));
    }

    public static QtfFunction compose(DoubleBinaryOperator func) {
        return new QtfFunction(2, p -> func.applyAsDouble(p.get(0), p.get(1)));
    }

    public static QtfFunction compose(DoubleTernaryOperator func) {
        return new QtfFunction(3, p -> func.applyAsDouble(p.get(0), p.get(1), p.get(2)));
    }

    @FunctionalInterface
    public interface Func {
        double apply(Parameters parameters) throws QtfExecutionException;
    }

    @FunctionalInterface
    public interface Parameters {
        double get(int index) throws QtfExecutionException;
    }
}
