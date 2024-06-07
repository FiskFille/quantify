package com.fiskmods.quantify.insn;

public class InsnNode {
    public final int instruction;
    public int index;

    public InsnNode(int instruction) {
        this.instruction = instruction;
    }

    @Override
    public String toString() {
        return Instruction.toString(instruction);
    }
}
