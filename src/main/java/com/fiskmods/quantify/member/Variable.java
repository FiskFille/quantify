package com.fiskmods.quantify.member;

public class Variable implements VarReference {
    private VarReference reference = VarReference.EMPTY;

    private Variable() {
    }

    public static Variable create() {
        return new Variable();
    }

    @Override
    public double get() {
        return reference.get();
    }

    @Override
    public void set(double value) {
        reference.set(value);
    }

    @Override
    public boolean isEmpty() {
        return reference.isEmpty();
    }

    public static QtfListener.Resolver resolve(MemberMap members, QtfMemory memory) {
        return (var, name) -> var.reference
                = memory.resolve(members.getVariableId(name));
    }
}
