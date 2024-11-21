package com.fiskmods.quantify;

import com.fiskmods.quantify.jvm.JvmRunnable;
import com.fiskmods.quantify.member.QtfMemory;

import java.util.Collections;
import java.util.Map;

public final class QtfScript {
    private final JvmRunnable runnable;
    private final QtfMemory memory;
    private final Map<String, Integer> inputs;

    QtfScript(JvmRunnable runnable, QtfMemory memory, Map<String, Integer> inputs) {
        this.runnable = runnable;
        this.memory = memory;
        this.inputs = Collections.unmodifiableMap(inputs);
    }

    public void run(double... args) {
        memory.run(runnable, args);
    }

    public void print() {
        memory.print();
    }

    public Map<String, Integer> getInputs() {
        return inputs;
    }
}
