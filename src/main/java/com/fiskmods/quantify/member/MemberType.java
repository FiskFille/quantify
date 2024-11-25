package com.fiskmods.quantify.member;

import java.util.Locale;

public enum MemberType {
    UNKNOWN,

    VARIABLE,
    CONSTANT,
    LIBRARY,
    OUTPUT,
    OUTPUT_VARIABLE;

    public boolean isGlobal() {
        return this == VARIABLE || this == CONSTANT;
    }

    public String toString() {
        return name().toLowerCase(Locale.ROOT);
    }
}
