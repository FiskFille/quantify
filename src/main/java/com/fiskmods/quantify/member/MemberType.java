package com.fiskmods.quantify.member;

import java.util.Locale;

public enum MemberType {
    VARIABLE,
    FUNCTION,
    LIBRARY,
    OUTPUT,
    OUTPUT_VARIABLE;

    public String toString() {
        return name().toLowerCase(Locale.ROOT);
    }
}
