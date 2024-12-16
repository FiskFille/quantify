package com.fiskmods.quantify.member;

import com.fiskmods.quantify.exception.QtfException;
import com.fiskmods.quantify.jvm.FunctionAddress;
import com.fiskmods.quantify.jvm.VarAddress;
import com.fiskmods.quantify.library.QtfLibrary;

public interface Namespace {
    VarAddress computeVariable(String name, boolean isDefinition) throws QtfException;

    boolean hasVariable(String name);

    FunctionAddress getFunction(String name) throws QtfException;

    boolean hasFunction(String name);

    double getConstant(String name) throws QtfException;

    boolean hasConstant(String name);

    static Namespace of(QtfLibrary library) {
        return new Namespace() {
            @Override
            public VarAddress computeVariable(String name, boolean isDefinition) throws QtfException {
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
            public boolean hasFunction(String name) {
                return getFunction(name) != null;
            }

            @Override
            public double getConstant(String name) {
                return library.getConstant(name);
            }

            @Override
            public boolean hasConstant(String name) {
                return library.getConstant(name) != null;
            }
        };
    }
}
