package com.fiskmods.quantify.member;

import java.util.Locale;

public enum MemberType {
    UNKNOWN,

    VARIABLE,
    LIBRARY,
    OUTPUT,
    OUTPUT_VARIABLE;

    public String toString() {
        return name().toLowerCase(Locale.ROOT);
    }
}
