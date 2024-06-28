package com.fiskmods.quantify.interpreter;

import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.insn.Instruction;
import com.fiskmods.quantify.insn.MemberInsnNode;
import com.fiskmods.quantify.insn.NamespaceInsnNode;
import com.fiskmods.quantify.insn.WithInsnNode;
import com.fiskmods.quantify.member.MemberType;
import com.fiskmods.quantify.util.QtfUtil;
import com.fiskmods.quantify.util.TokenReader;

import static com.fiskmods.quantify.Keywords.*;

public enum KeywordInterpreter implements Interpreter {
    INSTANCE;

    @Override
    public boolean interpret(TokenReader reader, InterpreterStack stack) throws QtfParseException {
        return switch (reader.nextToken()) {
            case DEF -> {
                String name = QtfUtil.extractName(reader);
                stack.add(new MemberInsnNode(Instruction.DEF,
                        stack.state().addMember(name, MemberType.VARIABLE, InterpreterState.ScopeLevel.LOCAL)
                ));
                yield true;
            }
            case IMPORT -> {
                String key = QtfUtil.extractString(reader);
                QtfUtil.consumeChar(reader, ':');
                String name = QtfUtil.extractName(reader);
                stack.state().addLibrary(name, key);
                yield true;
            }
            case INPUT -> {
                QtfUtil.consumeChar(reader, '[');
                int index = QtfUtil.extractInteger(reader);
                QtfUtil.consumeChar(reader, ']');
                QtfUtil.consumeChar(reader, ':');

                String name = QtfUtil.extractName(reader);
                int id = stack.state().addMember(name, MemberType.VARIABLE, InterpreterState.ScopeLevel.GLOBAL);

                stack.add(new MemberInsnNode(Instruction.DEF, id));
                stack.add(Instruction.EQ);
                stack.add(new MemberInsnNode(Instruction.IN, index));
                yield true;
            }
            case OUTPUT -> {
                QtfUtil.consumeChar(reader, ':');
                String name = QtfUtil.extractName(reader);

                reader.mark();
                stack.state().addMember(name, MemberType.OUTPUT, InterpreterState.ScopeLevel.GLOBAL);
                yield true;
            }
            case IF -> {
                if (stack.wasLast(Instruction.NSP)) {
                    stack.removeLast();
                }
                else {
                    stack.state().push();
                }
                stack.add(Instruction.IF);
                yield true;
            }
            case WITH -> {
                if (stack.wasLast(Instruction.NSP)) {
                    stack.removeLast();
                }
                else {
                    stack.state().push();
                }
                stack.add(new WithInsnNode(
                        stack.state().addMember(WITH, MemberType.VARIABLE, InterpreterState.ScopeLevel.LOCAL)));
                yield true;
            }
            case USING -> {
                String name = QtfUtil.extractName(reader);
                int id = stack.state().getMemberId(name, MemberType.LIBRARY, InterpreterState.ScopeLevel.GLOBAL);

                stack.state().push();
                stack.state().scope().setNamespace(id);
                stack.add(new NamespaceInsnNode(id));
                yield true;
            }
            case END -> {
                stack.add(Instruction.END);
                if (!stack.state().pop()) {
                    throw QtfParseException.invalidToken(stack.reader, "can't pop empty stack");
                }
                yield true;
            }
            default -> false;
        };
    }
}
