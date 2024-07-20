package com.fiskmods.quantify.script;

import com.fiskmods.quantify.jvm.JvmRunnable;
import com.fiskmods.quantify.member.QtfMemory;

import java.util.Collections;
import java.util.Map;

public class QtfScript {
    private final JvmRunnable runnable;
    private final QtfMemory memory;
    private final Map<String, Integer> inputs;

    public QtfScript(JvmRunnable runnable, QtfMemory memory, Map<String, Integer> inputs) {
        this.runnable = runnable;
        this.memory = memory;
        this.inputs = inputs;
    }

    public void run(double... args) {
        memory.run(runnable, args);
    }

    public void print() {
        memory.print();
    }

    public Map<String, Integer> getInputs() {
        return Collections.unmodifiableMap(inputs);
    }
}
