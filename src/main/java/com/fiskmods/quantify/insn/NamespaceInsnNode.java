package com.fiskmods.quantify.insn;

public class NamespaceInsnNode extends InsnNode {
    public final int id;

    public NamespaceInsnNode(int id) {
        super(Instruction.NSP);
        this.id = id;
    }

    @Override
    public String toString() {
        return String.format("%s{%d}", Instruction.toString(instruction), id);
    }
}
