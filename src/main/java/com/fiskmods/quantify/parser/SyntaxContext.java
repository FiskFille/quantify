package com.fiskmods.quantify.parser;

import com.fiskmods.quantify.QtfCompiler;
import com.fiskmods.quantify.exception.QtfException;
import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.jvm.FunctionAddress;
import com.fiskmods.quantify.jvm.VariableType;
import com.fiskmods.quantify.library.QtfLibrary;
import com.fiskmods.quantify.member.*;
import com.fiskmods.quantify.parser.element.VariableRef;

import java.util.*;
import java.util.function.UnaryOperator;

public class SyntaxContext {
    private final Namespace defaultNamespace = new DefaultNamespace();

    private final Scope globalScope = new Scope(defaultNamespace);
    private final LinkedList<Scope> currentScope = new LinkedList<>();
    private final List<Double> constants = new ArrayList<>();

    private final Map<String, Integer> inputs = new HashMap<>();
    private final String[] libraryKeys;

    private final QtfCompiler compiler;

    public SyntaxContext(QtfCompiler compiler) {
        this.compiler = compiler;
        libraryKeys = new String[compiler.libraries()];
        currentScope.add(globalScope);
    }

    public Namespace namespace() {
        return scope().getNamespace();
    }

    public Namespace getDefaultNamespace() {
        return defaultNamespace;
    }

    public MemberType typeOf(String name) {
        return scope().getType(name);
    }

    public Scope scope() {
        return currentScope.getLast();
    }

    public void push(UnaryOperator<Scope> scope) {
        currentScope.add(scope.apply(scope()));
    }

    public void pop() {
        if (currentScope.size() > 1) {
            currentScope.removeLast();
        }
    }

    public void addInput(String name, int index) {
        inputs.put(name, index);
    }

    public void addConstant(String name, double value) throws QtfParseException {
        int id = addMember(name, MemberType.CONSTANT);
        if (id == constants.size()) {
            constants.add(value);
        } else {
            constants.set(id, value);
        }
    }

    private Scope getScopeFor(MemberType type) {
        return type.isGlobal() ? scope() : globalScope;
    }

    public int addMember(String name, MemberType type) throws QtfParseException {
        try {
            return getScopeFor(type).put(name, type);
        } catch (QtfException e) {
            throw new QtfParseException(e);
        }
    }

    public int getMemberId(String name, MemberType expectedType) throws QtfParseException {
        try {
            return getScopeFor(expectedType).get(name, expectedType);
        } catch (QtfException e) {
            throw new QtfParseException(e);
        }
    }

    public boolean hasMember(String name, MemberType expectedType) {
        return getScopeFor(expectedType).has(name, expectedType);
    }

    public void addLibrary(String name, String key) throws QtfParseException {
        if (compiler.getLibrary(key) == null) {
            throw new QtfParseException("Unknown library '%s'".formatted(key));
        }
        int id = addMember(name, MemberType.LIBRARY);
        libraryKeys[id] = key;
    }

    public QtfLibrary getLibrary(int id) {
        return compiler.getLibrary(libraryKeys[id]);
    }

    public QtfLibrary getLibrary(String name) throws QtfParseException {
        int id = getMemberId(name, MemberType.LIBRARY);
        return getLibrary(id);
    }

    public Map<String, Integer> getInputs() {
        return inputs;
    }

    public List<String> getOutputs() {
        return globalScope.getIdMap(MemberType.OUTPUT_VARIABLE);
    }

    public QtfMemory createMemory(QtfListener listener) throws QtfParseException {
        if (currentScope.size() > 1) {
            throw new QtfParseException("Unbalanced stack: " + currentScope.size());
        }
        List<String> outputs = getOutputs();
        QtfMemory memory = new QtfMemory(new double[outputs.size()]);
        listener.listen(Variable.resolve(this, memory), outputs::stream);
        return memory;
    }

    private class DefaultNamespace implements Namespace {
        @Override
        public VariableRef computeVariable(String name, boolean isDefinition) throws QtfException {
            int id;
            if (isDefinition) {
                id = scope().put(name, MemberType.VARIABLE);
            } else {
                id = scope().get(name, MemberType.VARIABLE);
            }
            return new VariableRef(id, VariableType.LOCAL);
        }

        @Override
        public boolean hasVariable(String name) {
            return scope().has(name, MemberType.VARIABLE);
        }

        @Override
        public FunctionAddress getFunction(String name) {
            return null;
        }

        @Override
        public boolean hasFunction(String name) {
            return false;
        }

        @Override
        public double getConstant(String name) throws QtfException {
            int id = scope().get(name, MemberType.CONSTANT);
            return constants.get(id);
        }

        @Override
        public boolean hasConstant(String name) {
            return scope().has(name, MemberType.CONSTANT);
        }
    }
}
