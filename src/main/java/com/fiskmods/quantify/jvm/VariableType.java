package com.fiskmods.quantify.jvm;

import static com.fiskmods.quantify.insn.Instruction.IN;
import static com.fiskmods.quantify.insn.Instruction.OUT;

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

    public static VariableType get(int instruction) {
        return switch (instruction) {
            case IN -> INPUT;
            case OUT -> OUTPUT;
            default -> LOCAL;
        };
    }
}
