package com.fiskmods.quantify.insn;

import java.util.Arrays;

public class FuncDefInsnNode extends InsnNode {
    public final String name;
    public final int id;

    public final String[] parameters;

    public FuncDefInsnNode(String name, int id, String[] parameters) {
        super(Instruction.FDEF);
        this.name = name;
        this.id = id;
        this.parameters = parameters;
    }

    @Override
    public String toString() {
        return Instruction.toString(instruction) + "{'" + name + "'|" + id
                + "|" + Arrays.toString(parameters) + "}";
    }
}
