package com.fiskmods.quantify.library;

import com.fiskmods.quantify.exception.QtfException;
import com.fiskmods.quantify.jvm.FunctionAddress;
import com.fiskmods.quantify.jvm.VarAddress;
import com.fiskmods.quantify.jvm.assignable.VarType;
import com.fiskmods.quantify.member.Namespace;
import com.fiskmods.quantify.parser.element.Assignable;
import com.fiskmods.quantify.parser.element.Value;

public record FallbackNamespace(Namespace namespace, Namespace fallback) implements Namespace  {
    @Override
    public <T extends Value & Assignable> VarAddress<T> computeVariable(
            VarType<T> type, String name, boolean isDefinition) throws QtfException {
        if (isDefinition || namespace.hasVariable(name)) {
            return namespace.computeVariable(type, name, isDefinition);
        }
        return fallback.computeVariable(type, name, false);
    }

    @Override
    public boolean hasVariable(String name) {
        return namespace.hasVariable(name) || fallback.hasVariable(name);
    }

    @Override
    public FunctionAddress getFunction(String name) throws QtfException {
        if (namespace.hasFunction(name)) {
            return namespace.getFunction(name);
        }
        return fallback.getFunction(name);
    }

    @Override
    public boolean hasFunction(String name) {
        return namespace.hasFunction(name) || fallback.hasFunction(name);
    }

    @Override
    public double getConstant(String name) throws QtfException {
        if (namespace.hasConstant(name)) {
            return namespace.getConstant(name);
        }
        return fallback.getConstant(name);
    }

    @Override
    public boolean hasConstant(String name) {
        return namespace.hasConstant(name) || fallback.hasConstant(name);
    }
}
