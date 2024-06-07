package com.fiskmods.quantify.assembly;

import com.fiskmods.quantify.insn.InsnNode;

import java.util.List;

@FunctionalInterface
public interface AssemblerFactory {
    Assembler get(List<InsnNode> line);

    static AssemblerFactory singleton(Assembler assembler) {
        return t -> assembler;
    }
}
