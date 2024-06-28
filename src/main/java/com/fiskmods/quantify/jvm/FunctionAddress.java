package com.fiskmods.quantify.jvm;

public class FunctionAddress {
    public final String owner;
    public final String name;
    public final String descriptor;
    public final int parameters;

    public FunctionAddress(String owner, String name, int parameters) {
        this.owner = owner;
        this.name = name;
        this.descriptor = "(" + "D".repeat(parameters) + ")D";
        this.parameters = parameters;
    }
}
