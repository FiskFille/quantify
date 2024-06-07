package com.fiskmods.quantify.assembly;

import com.fiskmods.quantify.exception.QtfAssemblyException;

import static com.fiskmods.quantify.insn.Instruction.BND;
import static com.fiskmods.quantify.insn.Instruction.BST;

public class BracketMatcher {
    private int balance;
    private int start = -1;

    public int match(int i, int instruction, ResultConsumer resultConsumer) throws QtfAssemblyException {
        if (instruction == BST) {
            if (balance == 0) {
                start = i;
            }
            ++balance;
        } else if (instruction == BND && --balance == 0) {
            resultConsumer.accept(start, i);
            i = start;
            start = -1;
        }
        return i;
    }

    @FunctionalInterface
    public interface ResultConsumer {
        void accept(int start, int end) throws QtfAssemblyException;
    }
}
