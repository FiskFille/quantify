package com.fiskmods.quantify.script;

import com.fiskmods.quantify.assembly.AssemblyRunnable;
import com.fiskmods.quantify.exception.QtfExecutionException;
import com.fiskmods.quantify.member.QtfMemory;

public class QtfScript {
    private final AssemblyRunnable runnable;
    private final QtfMemory memory;

    public QtfScript(AssemblyRunnable runnable, QtfMemory memory) {
        this.runnable = runnable;
        this.memory = memory;
    }

    public void run() throws QtfExecutionException {
        runnable.run(memory);
    }

    public void run(double... args) throws QtfExecutionException {
        memory.loadInputs(args);
        runnable.run(memory);
    }

    public void print() {
        memory.print();
    }
}
