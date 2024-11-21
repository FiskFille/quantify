package com.fiskmods.quantify.jvm;

public enum VariableType {
    LOCAL,
    INPUT,
    OUTPUT;

    public boolean isExternal() {
        return this != LOCAL;
    }

    public int refOffset() {
        return ordinal();
    }
}
