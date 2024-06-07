package com.fiskmods.quantify.interpreter;

import com.fiskmods.quantify.Keywords;
import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.insn.LerpInsnNode;
import com.fiskmods.quantify.member.MemberType;
import com.fiskmods.quantify.util.TokenReader;

import static com.fiskmods.quantify.insn.Instruction.LRP;
import static com.fiskmods.quantify.insn.Instruction.RLRP;

public enum LerpInterpreter implements Interpreter {
    INSTANCE;

    @Override
    public boolean interpret(TokenReader reader, InterpreterStack stack) throws QtfParseException {
        boolean flag = "->".equals(reader.peek(2));
        if (!flag && !"-'>".equals(reader.peek(3))
                || !stack.state().scope().has(Keywords.WITH, MemberType.VARIABLE)) {
            return false;
        }
        reader.skip(flag ? 2 : 3);
        stack.add(new LerpInsnNode(flag ? LRP : RLRP,
                stack.state().getMemberId(Keywords.WITH, MemberType.VARIABLE, InterpreterState.ScopeLevel.LOCAL)));
        return true;
    }
}
