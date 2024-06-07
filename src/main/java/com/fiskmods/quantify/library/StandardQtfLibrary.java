package com.fiskmods.quantify.library;

import com.fiskmods.quantify.member.QtfFunction;
import com.fiskmods.quantify.util.DoubleTernaryOperator;

import java.util.HashMap;
import java.util.Map;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleSupplier;
import java.util.function.DoubleUnaryOperator;

public class StandardQtfLibrary implements QtfLibrary {
    private final String key;
    private final Map<String, Double> constants;
    private final Map<String, QtfFunction> functions;

    private StandardQtfLibrary(String key, Map<String, Double> constants, Map<String, QtfFunction> functions) {
        this.key = key;
        this.constants = constants;
        this.functions = functions;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public Double getConstant(String name) {
        return constants.get(name);
    }

    @Override
    public QtfFunction getFunction(String name) {
        return functions.get(name);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final Map<String, Double> constants = new HashMap<>();
        private final Map<String, QtfFunction> functions = new HashMap<>();

        public Builder addConstant(String name, double value) {
            functions.remove(name);
            constants.put(name, value);
            return this;
        }

        public Builder addFunction(String name, QtfFunction function) {
            constants.remove(name);
            functions.put(name, function);
            return this;
        }

        public Builder addFunction(String name, int parameters, QtfFunction.Func func) {
            constants.remove(name);
            functions.put(name, new QtfFunction(parameters, func));
            return this;
        }

        public Builder addFunction(String name, DoubleSupplier func) {
            return addFunction(name, QtfFunction.compose(func));
        }

        public Builder addFunction(String name, DoubleUnaryOperator func) {
            return addFunction(name, QtfFunction.compose(func));
        }

        public Builder addFunction(String name, DoubleBinaryOperator func) {
            return addFunction(name, QtfFunction.compose(func));
        }

        public Builder addFunction(String name, DoubleTernaryOperator func) {
            return addFunction(name, QtfFunction.compose(func));
        }

        public StandardQtfLibrary build(String key) {
            return new StandardQtfLibrary(key, constants, functions);
        }
    }
}
