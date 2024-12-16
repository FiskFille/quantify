package com.fiskmods.quantify.parser;

import com.fiskmods.quantify.QtfCompiler;
import com.fiskmods.quantify.exception.QtfException;
import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.jvm.FunctionAddress;
import com.fiskmods.quantify.jvm.JvmClassComposer;
import com.fiskmods.quantify.jvm.JvmFunctionDefinition;
import com.fiskmods.quantify.jvm.VarAddress;
import com.fiskmods.quantify.library.QtfLibrary;
import com.fiskmods.quantify.member.*;

import java.util.*;

public class SyntaxContext implements ScopeProvider {
    private static final int INPUT_ID = 1;
    private static final int OUTPUT_ID = 2;

    private final Namespace defaultNamespace = new DefaultNamespace();

    private final Scope globalScope = new Scope(defaultNamespace, 0);
    private final LinkedList<Scope> currentScope = new LinkedList<>();

    private final Map<String, Integer> inputs = new HashMap<>();
    private final List<String> outputs = new ArrayList<>();
    private final List<JvmFunctionDefinition> functionDefinitions = new ArrayList<>();

    private final QtfCompiler compiler;

    public SyntaxContext(QtfCompiler compiler) {
        this.compiler = compiler;
        currentScope.add(globalScope);
    }

    public Namespace namespace() {
        return scope().getNamespace();
    }

    public Namespace getDefaultNamespace() {
        return defaultNamespace;
    }

    @Override
    public Scope scope() {
        return currentScope.getLast();
    }

    @Override
    public Scope global() {
        return globalScope;
    }

    @Override
    public void push(Scope scope) {
        currentScope.add(scope);
    }

    @Override
    public void pop() {
        if (currentScope.size() > 1) {
            currentScope.removeLast();
        }
    }

    public void addLibrary(String name, String key) throws QtfParseException {
        QtfLibrary library = compiler.getLibrary(key);
        if (library == null) {
            throw new QtfParseException("Unknown library '%s'".formatted(key));
        }
        addMember(name, MemberType.LIBRARY, library);
    }

    public VarAddress addInputVariable(String name, int index) throws QtfParseException {
        try {
            VarAddress var = globalScope.members.<VarAddress> put("in:" + name,
                    MemberType.VARIABLE, () -> VarAddress.arrayAccess(INPUT_ID, index));
            inputs.put(name, index);
            return var;
        } catch (QtfException e) {
            throw new QtfParseException(e);
        }
    }

    public VarAddress addOutputVariable(String name) throws QtfParseException {
        try {
            VarAddress var = globalScope.members.<VarAddress> put(name,
                    MemberType.VARIABLE, () -> VarAddress.arrayAccess(OUTPUT_ID, outputs.size()));
            outputs.add(name);
            return var;
        } catch (QtfException e) {
            throw new QtfParseException(e);
        }
    }

    public int defineFunction(JvmFunctionDefinition composer) {
        functionDefinitions.add(composer);
        return functionDefinitions.size() - 1;
    }

    public Map<String, Integer> getInputs() {
        return inputs;
    }

    public List<String> getOutputs() {
        return outputs;
    }

    public JvmClassComposer createClassComposer(String className) {
        return functionDefinitions.stream()
                .map(t -> t.define(className))
                .reduce(JvmClassComposer.DO_NOTHING, JvmClassComposer::andThen);
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
        public VarAddress computeVariable(String name, boolean isDefinition) throws QtfException {
            if (isDefinition) {
                return addLocalVariable(name);
            }
            return getMember(name, MemberType.VARIABLE);
        }

        @Override
        public boolean hasVariable(String name) {
            return hasMember(name, MemberType.VARIABLE);
        }

        @Override
        public FunctionAddress getFunction(String name) throws QtfException {
            return getMember(name, MemberType.FUNCTION);
        }

        @Override
        public boolean hasFunction(String name) {
            return hasMember(name, MemberType.FUNCTION);
        }

        @Override
        public double getConstant(String name) throws QtfException {
            return getMember(name, MemberType.CONSTANT);
        }

        @Override
        public boolean hasConstant(String name) {
            return hasMember(name, MemberType.CONSTANT);
        }
    }
}
