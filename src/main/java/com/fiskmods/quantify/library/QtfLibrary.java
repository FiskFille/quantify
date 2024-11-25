package com.fiskmods.quantify.library;

import com.fiskmods.quantify.jvm.FunctionAddress;

public interface QtfLibrary {
    String getKey();

    FunctionAddress getFunction(String name);

    default boolean hasFunction(String name) {
        return getFunction(name) != null;
    }

    Double getConstant(String name);
}
