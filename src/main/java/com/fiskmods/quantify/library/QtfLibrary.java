package com.fiskmods.quantify.library;

import com.fiskmods.quantify.member.QtfFunction;

public interface QtfLibrary {
    String getKey();

    Double getConstant(String name);

    QtfFunction getFunction(String name);
}
