package com.fiskmods.quantify.member;

import com.fiskmods.quantify.exception.QtfException;
import com.fiskmods.quantify.jvm.FunctionAddress;
import com.fiskmods.quantify.library.QtfLibrary;
import com.fiskmods.quantify.parser.element.VariableRef;

public interface Namespace {
    VariableRef computeVariable(String name, boolean isDefinition) throws QtfException;

    boolean hasVariable(String name);

    FunctionAddress getFunction(String name);

    default boolean hasFunction(String name) {
        return getFunction(name) != null;
    }

    Double getConstant(String name);

    default boolean hasConstant(String name) {
        return getConstant(name) != null;
    }

    static Namespace of(QtfLibrary library) {
        return new Namespace() {
            @Override
            public VariableRef computeVariable(String name, boolean isDefinition) throws QtfException {
                throw new QtfException("Undefined variable '%s' in library '%s'"
                        .formatted(name, library.getKey()));
            }

            @Override
            public boolean hasVariable(String name) {
                return false;
            }

            @Override
            public FunctionAddress getFunction(String name) {
                return library.getFunction(name);
            }

            @Override
            public Double getConstant(String name) {
                return library.getConstant(name);
            }
        };
    }
}
