package com.fiskmods.quantify.jvm;

import com.fiskmods.quantify.lexer.token.Operator;
import com.fiskmods.quantify.parser.element.Assignable;
import com.fiskmods.quantify.parser.element.Value;
import org.objectweb.asm.MethodVisitor;

public interface VarAddress extends Value, Assignable {
    JvmVariable access();

    boolean isNegated();

    @Override
    default void apply(MethodVisitor mv) {
        access().apply(mv);
    }

    @Override
    default void modify(MethodVisitor mv, Value value, Operator operator) {
        access().modify(mv, value, operator);
    }

    @Override
    default void set(MethodVisitor mv, Value value) {
        access().set(mv, value);
    }

    @Override
    default void lerp(MethodVisitor mv, Value value, Value progress, boolean rotational) {
        access().lerp(mv, value, progress, rotational);
    }

    static VarAddress create(JvmVariable access, boolean isNegated) {
        return new Impl(access, isNegated);
    }

    static VarAddress local(int id) {
        return new Impl(new JvmVariable.Local(id), false);
    }

    static VarAddress arrayAccess(int id, int arrayIndex) {
        return new Impl(new JvmVariable.ArrayAccess(id, arrayIndex), false);
    }

    record Impl(JvmVariable access, boolean isNegated) implements VarAddress {
    }
}
