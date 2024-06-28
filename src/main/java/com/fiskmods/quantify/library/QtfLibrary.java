package com.fiskmods.quantify.library;

import com.fiskmods.quantify.jvm.FunctionAddress;

public interface QtfLibrary {
    String getKey();

    Double getConstant(String name);

    FunctionAddress getFunction(String name);
}
