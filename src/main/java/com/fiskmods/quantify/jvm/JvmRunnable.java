package com.fiskmods.quantify.jvm;

@FunctionalInterface
public interface JvmRunnable {
    void run(double[] input, double[] output);
}
