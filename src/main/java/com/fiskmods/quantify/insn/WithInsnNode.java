package com.fiskmods.quantify.insn;

public class WithInsnNode extends InsnNode {
    public final int id;

    public WithInsnNode(int id) {
        super(Instruction.WTH);
        this.id = id;
    }

    @Override
    public String toString() {
        return String.format("%s{%d}", Instruction.toString(instruction), id);
    }
}
