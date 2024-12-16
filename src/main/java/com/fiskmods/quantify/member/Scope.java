package com.fiskmods.quantify.member;

import com.fiskmods.quantify.exception.QtfException;
import com.fiskmods.quantify.jvm.VarAddress;
import com.fiskmods.quantify.parser.element.Value;

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

    public VarAddress addLocalVariable(String name) throws QtfException {
        return members.<VarAddress> put(name, MemberType.VARIABLE, () -> {
            VarAddress var = VarAddress.local(localIndexOffset);
            localIndexOffset += 2;
            return var;
        });
    }

    public Struct addStruct(String name) throws QtfException {
        return members.<Struct> put(name, MemberType.STRUCT, () -> {
            Struct struct = new Struct(localIndexOffset);
            ++localIndexOffset;
            return struct;
        });
    }
}
