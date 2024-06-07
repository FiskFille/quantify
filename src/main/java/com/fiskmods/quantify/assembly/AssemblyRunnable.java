package com.fiskmods.quantify.assembly;

import com.fiskmods.quantify.exception.QtfAssemblyException;
import com.fiskmods.quantify.exception.QtfExecutionException;
import com.fiskmods.quantify.member.QtfMemory;

@FunctionalInterface
public interface AssemblyRunnable {
    AssemblyRunnable EMPTY = new AssemblyRunnable() {
        @Override
        public void run(QtfMemory memory) {
        }

        @Override
        public AssemblyRunnable andThen(AssemblyRunnable next) {
            return next;
        }
    };

    void run(QtfMemory memory) throws QtfExecutionException;

    default AssemblyRunnable andThen(AssemblyRunnable next) {
        return t -> {
            run(t);
            next.run(t);
        };
    }

    @FunctionalInterface
    interface Supplier {
        AssemblyRunnable get() throws QtfAssemblyException;
    }
}
