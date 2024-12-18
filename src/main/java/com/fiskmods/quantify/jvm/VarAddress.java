package com.fiskmods.quantify.jvm;

import com.fiskmods.quantify.exception.QtfException;
import com.fiskmods.quantify.jvm.assignable.NumVar;
import com.fiskmods.quantify.jvm.assignable.VarType;
import com.fiskmods.quantify.lexer.token.Operator;
import com.fiskmods.quantify.parser.element.Assignable;
import com.fiskmods.quantify.parser.element.Value;
import org.objectweb.asm.MethodVisitor;

public interface VarAddress<T extends Value & Assignable> extends Value, Assignable {
    VarType<T> type();

    default boolean is(VarType<?> type) {
        return type() == type;
    }

    T access();

    boolean isNegated();

    default void typeCheck(String name, VarType<?> expectedType) throws QtfException {
        if (expectedType != null && type() != expectedType) {
            throw new QtfException("Expected '%s' to be a %s, was %s"
                    .formatted(name, expectedType.name(), type().name()));
        }
    }

    @SuppressWarnings("unchecked")
    default <U extends Value & Assignable> VarAddress<U> cast(String name, VarType<U> expectedType) throws QtfException {
        typeCheck(name, expectedType);
        return (VarAddress<U>) this;
    }

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
    default void init(MethodVisitor mv) {
        access().init(mv);
    }

    @Override
    default void lerp(MethodVisitor mv, Value value, Value progress, boolean rotational) {
        access().lerp(mv, value, progress, rotational);
    }

    static <T extends Value & Assignable> VarAddress<T> create(VarType<T> type, T access, boolean isNegated) {
        return new Impl<>(type, access, isNegated);
    }

    static <T extends Value & Assignable> VarAddress<T> create(VarAddress<T> other, boolean isNegated) {
        return create(other.type(), other.access(), isNegated);
    }

    static VarAddress<NumVar> local(int id) {
        return new Impl<>(VarType.NUM, new NumVar.Local(id), false);
    }

    static VarAddress<NumVar> arrayAccess(int id, int arrayIndex) {
        return new Impl<>(VarType.NUM, new NumVar.ArrayAccess(id, arrayIndex), false);
    }

    record Impl<T extends Value & Assignable>(VarType<T> type, T access, boolean isNegated)
            implements VarAddress<T> {
    }
}
