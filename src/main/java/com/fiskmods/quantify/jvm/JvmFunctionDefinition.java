package com.fiskmods.quantify.jvm;

@FunctionalInterface
public interface JvmFunctionDefinition {
    JvmClassComposer define(String className);
}
