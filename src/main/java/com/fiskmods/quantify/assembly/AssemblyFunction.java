package com.fiskmods.quantify.assembly;

import com.fiskmods.quantify.exception.QtfExecutionException;
import com.fiskmods.quantify.member.QtfMemory;

@FunctionalInterface
public interface AssemblyFunction {
    double apply(QtfMemory memory) throws QtfExecutionException;

    default boolean applyAsBoolean(QtfMemory memory) throws QtfExecutionException {
        return apply(memory) > 0;
    }
}
