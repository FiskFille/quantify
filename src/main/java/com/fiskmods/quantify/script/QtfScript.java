package com.fiskmods.quantify.script;

import com.fiskmods.quantify.exception.QtfExecutionException;
import com.fiskmods.quantify.jvm.JvmRunnable;
import com.fiskmods.quantify.member.QtfMemory;

public class QtfScript {
    private final JvmRunnable runnable;
    private final QtfMemory memory;

    public QtfScript(JvmRunnable runnable, QtfMemory memory) {
        this.runnable = runnable;
        this.memory = memory;
    }

    public void run(double... args) throws QtfExecutionException {
        memory.run(runnable, args);
    }

    public void print() {
        memory.print();
    }
}
