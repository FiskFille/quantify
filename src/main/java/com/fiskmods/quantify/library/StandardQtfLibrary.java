package com.fiskmods.quantify.library;

import com.fiskmods.quantify.jvm.FunctionAddress;

import java.util.HashMap;
import java.util.Map;

public class StandardQtfLibrary implements QtfLibrary {
    private final String key;
    private final Map<String, FunctionAddress> functions;
    private final Map<String, Double> constants;

    private StandardQtfLibrary(String key, Map<String, Double> constants, Map<String, FunctionAddress> functions) {
        this.key = key;
        this.constants = constants;
        this.functions = functions;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public FunctionAddress getFunction(String name) {
        return functions.get(name);
    }

    @Override
    public Double getConstant(String name) {
        return constants.get(name);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final Map<String, FunctionAddress> functions = new HashMap<>();
        private final Map<String, Double> constants = new HashMap<>();

        public Builder addConstant(String name, double value) {
            functions.remove(name);
            constants.put(name, value);
            return this;
        }

        public Builder addFunction(String name, FunctionAddress function) {
            constants.remove(name);
            functions.put(name, function);
            return this;
        }

        public Builder addFunction(String owner, String name, int parameters) {
            constants.remove(name);
            functions.put(name, FunctionAddress.create(owner, name, parameters));
            return this;
        }

        public StandardQtfLibrary build(String key) {
            return new StandardQtfLibrary(key, constants, functions);
        }
    }
}
