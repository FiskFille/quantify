package com.fiskmods.quantify.member;

import com.fiskmods.quantify.jvm.FunctionAddress;
import com.fiskmods.quantify.library.QtfLibrary;
import com.fiskmods.quantify.parser.element.VariableRef;

public record MemberType<T>(String name, boolean isLocal) {
    public static final MemberType<VariableRef> VARIABLE = new MemberType<>("variable", true);
    public static final MemberType<Double> CONSTANT = new MemberType<>("constant", true);
    public static final MemberType<FunctionAddress> FUNCTION = new MemberType<>("function", true);

    public static final MemberType<QtfLibrary> LIBRARY = new MemberType<>("library", false);
    public static final MemberType<String> OUTPUT = new MemberType<>("output", false);

    public Scope scope(ScopeProvider provider) {
        return isLocal ? provider.scope() : provider.global();
    }
}
