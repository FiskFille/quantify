package com.fiskmods.quantify.insn;

import static com.fiskmods.quantify.insn.Instruction.getConstantValue;

public interface ValueInsnNode {
    boolean isNegative();

    InsnNode negated();

    static InsnNode negated(InsnNode node) {
        if (node instanceof ValueInsnNode v) {
            return v.negated();
        }
        if (!Instruction.isValue(node.instruction)) {
            throw new IllegalStateException("Can't invert non-value: " + node);
        }
        return new CstInsnNode(-getConstantValue(node.instruction));
    }
}
