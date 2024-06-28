package com.fiskmods.quantify.interpreter;

import com.fiskmods.quantify.Keywords;
import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.insn.CstInsnNode;
import com.fiskmods.quantify.insn.MemberInsnNode;
import com.fiskmods.quantify.library.QtfLibrary;
import com.fiskmods.quantify.member.MemberType;
import com.fiskmods.quantify.util.QtfUtil;
import com.fiskmods.quantify.util.TokenReader;

import static com.fiskmods.quantify.exception.QtfParseException.error;
import static com.fiskmods.quantify.insn.Instruction.*;

public class NameInterpreter implements Interpreter {
    private final boolean multiVar, libRef, func;

    public NameInterpreter(boolean multiVar, boolean libRef, boolean func) {
        this.multiVar = multiVar;
        this.libRef = libRef;
        this.func = func;
    }

    @Override
    public boolean interpret(TokenReader reader, InterpreterStack stack) throws QtfParseException {
        String name = reader.nextIdentifier();
        if (name == null) {
            return false;
        }
        if (name.equals(Keywords.THIS) && reader.hasNext() && reader.peekChar() == '.') {
            reader.skip(1);
            reader.mark();
            name = reader.nextIdentifier();
            if (name == null) {
                return false;
            }
            return tryInterpretMain(reader, stack, name, false);
        }
        if (tryInterpretConst(stack, name)) {
            return true;
        }

        QtfUtil.checkNameValid(reader, name);
        if (reader.hasNext() && tryInterpretLib(reader, stack, name)) {
            return true;
        }
        return tryInterpretMain(reader, stack, name, true);
    }

    private boolean tryInterpretMain(TokenReader reader, InterpreterStack stack, String name, boolean allowNamespace)
            throws QtfParseException {
        if (func && reader.hasNext() && tryInterpretFunc(reader)) {
            int id, namespace;
            if (allowNamespace && (namespace = stack.state().scope().getNamespace()) > -1) {
                id = stack.state().getLibraryFunction(namespace, name);
            }
            else {
                id = stack.state().getMemberId(name, MemberType.FUNCTION, InterpreterState.ScopeLevel.GLOBAL);
            }
            stack.add(new MemberInsnNode(trySkipFunc(reader) ? FRUN : FREF, id));
            return true;
        }

        if (multiVar && stack.wasLast(NXT) && stack.lookBack(2) instanceof MemberInsnNode var) {
            int id;
            if (var.instruction == DEF) {
                id = stack.state().addMember(name, MemberType.VARIABLE, InterpreterState.ScopeLevel.LOCAL);
            } else {
                id = stack.state().getMemberId(name, MemberType.VARIABLE, InterpreterState.ScopeLevel.LOCAL);
            }
            stack.add(new MemberInsnNode(var.instruction, id));
            return true;
        }

        int namespace;
        if (allowNamespace && (namespace = stack.state().scope().getNamespace()) > -1) {
            QtfLibrary library = stack.state().getLibrary(namespace);
            Double value = library.getConstant(name);
            if (value != null) {
                stack.add(new CstInsnNode(value));
                return true;
            }
        }

        int id = stack.state().getMemberId(name, MemberType.VARIABLE, InterpreterState.ScopeLevel.LOCAL);
        stack.add(new MemberInsnNode(REF, id));
        return true;
    }

    private boolean tryInterpretConst(InterpreterStack stack, String name) throws QtfParseException {
        int instruction = Keywords.getConstVar(name);
        if (instruction == -1) {
            return false;
        }
        stack.add(instruction);
        return true;
    }

    private boolean tryInterpretLib(TokenReader reader, InterpreterStack stack, String parentName) throws QtfParseException {
        if (reader.peekChar() != '.') {
            return false;
        }

        int libId = -1;
        int outId = -1;
        if (stack.state().has(parentName, MemberType.OUTPUT, InterpreterState.ScopeLevel.GLOBAL)) {
            outId = stack.state().getMemberId(parentName, MemberType.OUTPUT, InterpreterState.ScopeLevel.GLOBAL);
        } else {
            if (!libRef) {
                return false;
            }
            libId = stack.state().getMemberId(parentName, MemberType.LIBRARY, InterpreterState.ScopeLevel.GLOBAL);
        }

        reader.skip(1);
        reader.mark();
        String name;
        if (!reader.hasNext() || (name = reader.nextIdentifier()) == null) {
            throw QtfParseException.unexpectedTokenOrError(reader, "expected name");
        }

        if (outId != -1) {
            int id;
            name = extractOutputName(name, reader);

            if (stack.state().has(name, MemberType.OUTPUT_VARIABLE, InterpreterState.ScopeLevel.GLOBAL)) {
                id = stack.state().getMemberId(name, MemberType.OUTPUT_VARIABLE, InterpreterState.ScopeLevel.GLOBAL);
            } else {
                id = stack.state().addMember(name, MemberType.OUTPUT_VARIABLE, InterpreterState.ScopeLevel.GLOBAL);
            }
            stack.add(new MemberInsnNode(OUT, id));
            return true;
        }

        if (func && reader.hasNext() && tryInterpretFunc(reader)) {
            int id = stack.state().getLibraryFunction(libId, name);
            stack.add(new MemberInsnNode(trySkipFunc(reader) ? FRUN : FREF, id));
            return true;
        }

        QtfLibrary library = stack.state().getLibrary(libId);
        Double value = library.getConstant(name);
        if (value == null) {
            throw error(stack.reader, "no such constant '%s' in library '%s'"
                    .formatted(name, library.getKey()));
        }
        stack.add(new CstInsnNode(value));
        return true;
    }

    private String extractOutputName(String name, TokenReader reader) throws QtfParseException {
        StringBuilder nameBuilder = new StringBuilder(QtfUtil.OUTPUT_PREFIX + name);
        while (QtfUtil.peekChar(reader) == '.') {
            reader.skipSpaces();
            reader.skip(1);
            nameBuilder.append('.')
                    .append(QtfUtil.extractName(reader));
        }
        return nameBuilder.toString();
    }

    private boolean tryInterpretFunc(TokenReader reader) {
        int i = reader.getScanIndex();
        reader.skipSpaces();
        if (reader.peekChar() != '(') {
            return false;
        }
        reader.setScanIndex(i);
        return true;
    }

    private boolean trySkipFunc(TokenReader reader) {
        int i = reader.getScanIndex();
        reader.skipSpaces();
        reader.skip(1);
        reader.skipSpaces();
        if (!reader.hasNext() || reader.peekChar() != ')') {
            reader.setScanIndex(i);
            return false;
        }
        reader.skip(1);
        return true;
    }
}
