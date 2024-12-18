package com.fiskmods.quantify.member;

import com.fiskmods.quantify.exception.QtfException;
import com.fiskmods.quantify.jvm.VarAddress;
import com.fiskmods.quantify.jvm.assignable.NumVar;
import com.fiskmods.quantify.jvm.assignable.Struct;
import com.fiskmods.quantify.jvm.assignable.VarType;
import com.fiskmods.quantify.parser.element.Assignable;
import com.fiskmods.quantify.parser.element.Value;

import java.util.function.IntFunction;

public class Scope {
    public final MemberMap members = new MemberMap();
    protected final int level;

    protected Namespace namespace;
    protected Value lerpProgress;

    protected int localIndexOffset = 3;

    public Scope(Namespace namespace, int level) {
        this.namespace = namespace;
        this.level = level;
    }

    public Scope copy(Namespace namespace) {
        Scope scope = new Scope(namespace, level + 1);
        scope.lerpProgress = lerpProgress;
        scope.localIndexOffset = localIndexOffset;
        scope.members.inherit(members);
        return scope;
    }

    public Scope copy() {
        return copy(namespace);
    }

    public void setNamespace(Namespace namespace) {
        this.namespace = namespace;
    }

    public Namespace getNamespace() {
        return namespace;
    }

    public void setLerpProgress(Value lerpProgress) {
        this.lerpProgress = lerpProgress;
    }

    public Value getLerpProgress() {
        return lerpProgress;
    }

    public boolean isInnerScope() {
        return level > 0;
    }

    public <T extends Value & Assignable> VarAddress<T> addLocalVariable(
            String name, VarType<T> type, IntFunction<VarAddress<T>> supplier) throws QtfException {
        return members.put(name, () -> {
            VarAddress<T> var = supplier.apply(localIndexOffset);
            localIndexOffset += type.size();
            return var;
        });
    }

    public VarAddress<NumVar> addLocalVariable(String name) throws QtfException {
        return addLocalVariable(name, VarType.NUM, VarAddress::local);
    }

    public VarAddress<Struct> addStruct(String name) throws QtfException {
        return addLocalVariable(name, VarType.STRUCT, Struct::createVar);
    }
}
