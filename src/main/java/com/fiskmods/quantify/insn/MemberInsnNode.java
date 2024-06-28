package com.fiskmods.quantify.insn;

import com.fiskmods.quantify.jvm.VariableType;
import com.fiskmods.quantify.member.QtfMemory;

public class MemberInsnNode extends InsnNode implements ValueInsnNode {
    public int id;
    private boolean negated;

    public MemberInsnNode(int instruction, int id) {
        super(instruction);
        this.id = id;
    }

    @Override
    public boolean isNegative() {
        return negated;
    }

    @Override
    public InsnNode negated() {
        negated = !negated;
        return this;
    }

    public QtfMemory.Address toAddress() {
        return new QtfMemory.Address(id, VariableType.get(instruction), negated);
    }

    @Override
    public String toString() {
        String s = String.format("%s{%d}", Instruction.toString(instruction), id);
        return negated ? "-" + s : s;
    }
}
