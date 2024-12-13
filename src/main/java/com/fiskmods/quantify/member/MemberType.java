package com.fiskmods.quantify.member;

import java.util.Locale;

public enum MemberType {
    UNKNOWN,

    VARIABLE,
    CONSTANT,
    FUNCTION,
    LIBRARY,
    OUTPUT,
    OUTPUT_VARIABLE;

    public boolean isLocal() {
        return this == VARIABLE || this == CONSTANT || this == FUNCTION;
    }

    public String toString() {
        return name().toLowerCase(Locale.ROOT);
    }
}
