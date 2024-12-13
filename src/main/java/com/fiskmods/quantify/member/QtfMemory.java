package com.fiskmods.quantify.member;

import com.fiskmods.quantify.jvm.JvmRunnable;

public class QtfMemory {
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
}
