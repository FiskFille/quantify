package com.fiskmods.quantify.jvm;

import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.lexer.token.Token;
import com.fiskmods.quantify.parser.element.Value;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public interface FunctionAddress {
    String owner();
    String name();
    String descriptor();
    int parameters();

    // TODO
    default String getLoggingName() {
        return owner() + "::" + name() + descriptor();
    }

    default void visit(MethodVisitor mv, int opcode, boolean isInterface) {
        mv.visitMethodInsn(opcode, owner(), name(), descriptor(), isInterface);
    }

    default void run(MethodVisitor mv, Value[] args) {
        for (Value arg : args) {
            arg.apply(mv);
        }
        visit(mv, Opcodes.INVOKESTATIC, false);
    }

    default void validateParameters(int arguments, Token location) throws QtfParseException {
        if (arguments != parameters()) {
            throw new QtfParseException("Incorrect number of arguments for " + getLoggingName(),
                    "expected %d, was %d".formatted(parameters(), arguments), location);
        }
    }

    static String descriptor(int parameters) {
        return "(" + "D".repeat(parameters) + ")D";
    }

    static FunctionAddress create(String owner, String name, int parameters) {
        return new Impl(owner, name, parameters);
    }

    record Impl(String owner, String name, String descriptor, int parameters) implements FunctionAddress {
        public Impl(String owner, String name, int parameters) {
            this(owner, name, FunctionAddress.descriptor(parameters), parameters);
        }
    }
}
