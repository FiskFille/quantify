package com.fiskmods.quantify.util;

@FunctionalInterface
public interface DoubleTernaryOperator {
    double applyAsDouble(double left, double middle, double right);
}
