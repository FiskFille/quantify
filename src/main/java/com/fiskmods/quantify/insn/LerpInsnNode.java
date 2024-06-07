package com.fiskmods.quantify.insn;

public class LerpInsnNode extends InsnNode {
    public final int id;

    public LerpInsnNode(int instruction, int id) {
        super(instruction);
        this.id = id;
    }

    @Override
    public String toString() {
        return String.format("%s{%d}", Instruction.toString(instruction), id);
    }
}
