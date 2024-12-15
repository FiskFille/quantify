package com.fiskmods.quantify.jvm;

public enum VariableType {
    LOCAL,
    INPUT,
    OUTPUT,
    PARAM;

    public boolean isExternal() {
        return this == INPUT || this == OUTPUT;
    }

    public int refOffset() {
        return switch (this) {
            case INPUT -> 1;
            case OUTPUT -> 2;
            default -> throw new UnsupportedOperationException();
        };
    }
}
