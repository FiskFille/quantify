package com.fiskmods.quantify.member;

import com.fiskmods.quantify.exception.QtfException;
import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.jvm.VarAddress;
import com.fiskmods.quantify.jvm.assignable.NumVar;
import com.fiskmods.quantify.jvm.assignable.Struct;

import java.util.Optional;
import java.util.function.UnaryOperator;

public interface ScopeProvider {
    Scope scope();

    Scope global();

    void push(Scope scope);

    void pop();

    default void push(UnaryOperator<Scope> scope) {
        push(scope.apply(scope()));
    }

    default Optional<MemberMap.Member<?>> findMember(String name) {
        return scope().members.find(name);
    }

    default  <T> Optional<T> findMember(String name, MemberType<T> expectedType) {
        return expectedType.scope(this).members.find(name, expectedType);
    }

    default boolean hasMember(String name, MemberType<?> expectedType) {
        return expectedType.scope(this).members.has(name, expectedType);
    }

    default <T> void addMember(String name, MemberType<T> type, T value) throws QtfParseException {
        try {
            type.scope(this).members.put(name, type, value);
        } catch (QtfException e) {
            throw new QtfParseException(e);
        }
    }

    default VarAddress<NumVar> addLocalVariable(String name) throws QtfParseException {
        try {
            return scope().addLocalVariable(name);
        } catch (QtfException e) {
            throw new QtfParseException(e);
        }
    }

    default VarAddress<Struct> addStruct(String name) throws QtfParseException {
        try {
            return scope().addStruct(name);
        } catch (QtfException e) {
            throw new QtfParseException(e);
        }
    }

    default  <T> T getMember(String name, MemberType<T> expectedType) throws QtfParseException {
        try {
            return expectedType.scope(this).members.get(name, expectedType);
        } catch (QtfException e) {
            throw new QtfParseException(e);
        }
    }
}
