package com.fiskmods.quantify.insn;

public class CstInsnNode extends InsnNode implements ValueInsnNode {
    public final double value;

    protected CstInsnNode(int instruction, double value) {
        super(instruction);
        this.value = value;
    }

    public CstInsnNode(double value) {
        this(Instruction.CST, value);
    }

    @Override
    public boolean isNegative() {
        return value < 0;
    }

    @Override
    public InsnNode negated() {
        if (value == 0) {
            return this;
        }
        return new CstInsnNode(-value);
    }

    @Override
    public String toString() {
        return Instruction.toString(instruction) + "{" + value + "}";
    }
}
