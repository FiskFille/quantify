package com.fiskmods.quantify.insn;

import com.fiskmods.quantify.jvm.VariableType;
import com.fiskmods.quantify.member.QtfMemory;

public class LerpInsnNode extends InsnNode {
    public final int id;

    public LerpInsnNode(int instruction, int id) {
        super(instruction);
        this.id = id;
    }

    public QtfMemory.Address toAddress() {
        return new QtfMemory.Address(id, VariableType.LOCAL, false);
    }

    @Override
    public String toString() {
        return String.format("%s{%d}", Instruction.toString(instruction), id);
    }
}
