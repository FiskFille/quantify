package com.fiskmods.quantify.jvm.assignable;

import com.fiskmods.quantify.exception.QtfException;
import com.fiskmods.quantify.jvm.VarAddress;
import com.fiskmods.quantify.member.Scope;
import com.fiskmods.quantify.parser.element.Assignable;
import com.fiskmods.quantify.parser.element.Value;

public record VarType<T extends Value & Assignable>(String name, int size) {
    public static final VarType<NumVar> NUM = new VarType<>("number", 2);
    public static final VarType<Struct> STRUCT = new VarType<>("struct", 1);

    @SuppressWarnings("unchecked")
    public VarAddress<T> define(String name, Scope scope) throws QtfException {
        if (this == VarType.NUM) {
            return (VarAddress<T>) scope.addLocalVariable(name);
        }
        throw new QtfException("Unable to define %s '%s'"
                .formatted(this.name, name));
    }
}
