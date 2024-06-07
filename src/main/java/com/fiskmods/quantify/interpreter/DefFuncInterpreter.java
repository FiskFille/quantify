package com.fiskmods.quantify.interpreter;

import com.fiskmods.quantify.Keywords;
import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.insn.FuncDefInsnNode;
import com.fiskmods.quantify.member.MemberType;
import com.fiskmods.quantify.util.QtfUtil;
import com.fiskmods.quantify.util.TokenReader;

import java.util.ArrayList;
import java.util.List;

public enum DefFuncInterpreter implements Interpreter {
    INSTANCE;

    @Override
    public boolean interpret(TokenReader reader, InterpreterStack stack) throws QtfParseException {
        if (!Keywords.FUNC.equals(reader.nextToken())) {
            return false;
        }

        String name = QtfUtil.extractName(reader);
        List<String> params = new ArrayList<>();
        QtfUtil.consumeChar(reader, '(');

        while (true) {
            params.add(QtfUtil.extractName(reader));
            if (QtfUtil.peekChar(reader) != ',') {
                break;
            }
            QtfUtil.consumeChar(reader, ',');
        }

        QtfUtil.consumeChar(reader, ')');

        stack.add(new FuncDefInsnNode(name,
                stack.state().addMember(name, MemberType.FUNCTION, InterpreterState.ScopeLevel.GLOBAL),
                params.toArray(new String[0])));

        reader.skipSpaces();
        reader.mark();
        return true;
    }
}
