package com.fiskmods.quantify.parser;

import com.fiskmods.quantify.QtfCompiler;
import com.fiskmods.quantify.exception.QtfException;
import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.jvm.FunctionAddress;
import com.fiskmods.quantify.library.QtfLibrary;
import com.fiskmods.quantify.member.MemberMap;
import com.fiskmods.quantify.member.MemberType;
import com.fiskmods.quantify.member.Scope;

import java.util.*;

public class SyntaxContext {
    private final Scope globalScope = new Scope();
    private final List<Scope> allScopes = new ArrayList<>();
    private final LinkedList<Scope> currentScope = new LinkedList<>();

    private final Map<String, Integer> inputs = new HashMap<>();
    private final String[] libraryKeys;
    private FunctionAddress[] functions = new FunctionAddress[8];

    private final QtfCompiler compiler;

    public SyntaxContext(QtfCompiler compiler) {
        this.compiler = compiler;
        libraryKeys = new String[compiler.libraries()];
        currentScope.add(globalScope);
        allScopes.add(globalScope);
    }

    public MemberMap compileMembers() throws QtfParseException {
        if (currentScope.size() > 1) {
            throw new QtfParseException("Unbalanced stack: " + currentScope.size());
        }
        return new MemberMap(globalScope, functions, inputs);
    }

    public Scope scope() {
        return currentScope.getLast();
    }

    public void push() {
        currentScope.add(scope().copy());
    }

    public int id() {
        return currentScope.size() + allScopes.size() - 2;
    }

    public void pop() {
        if (currentScope.size() > 1) {
            allScopes.add(currentScope.removeLast());
        }
    }

    public void addInput(String name, int index) {
        inputs.put(name, index);
    }

    public int addMember(String name, MemberType type, ScopeLevel level) throws QtfParseException {
        try {
            return level.get(this).put(name, type);
        } catch (QtfException e) {
            throw new QtfParseException(e);
        }
    }

    public int getMemberId(String name, MemberType expectedType, ScopeLevel level) throws QtfParseException {
        try {
            return level.get(this).get(name, expectedType);
        } catch (QtfException e) {
            throw new QtfParseException(e);
        }
    }

    public boolean has(String name, MemberType expectedType, ScopeLevel level) {
        return level.get(this).has(name, expectedType);
    }

    public void addLibrary(String name, String key) throws QtfParseException {
        if (compiler.getLibrary(key) == null) {
            throw new QtfParseException("Unknown library '%s'".formatted(key));
        }
        int id = addMember(name, MemberType.LIBRARY, ScopeLevel.GLOBAL);
        libraryKeys[id] = key;
    }

    public QtfLibrary getLibrary(int id) {
        return compiler.getLibrary(libraryKeys[id]);
    }

    public int addFunction(String name, FunctionAddress function) throws QtfParseException {
        int id = addMember(name, MemberType.FUNCTION, ScopeLevel.GLOBAL);
        if (id >= functions.length) {
            FunctionAddress[] array = functions;
            functions = new FunctionAddress[Math.max(array.length * 2, id + 1)];
            System.arraycopy(array, 0, functions, 0, array.length);
        }
        functions[id] = function;
        return id;
    }

    public enum ScopeLevel {
        LOCAL, GLOBAL;
        public Scope get(SyntaxContext context) {
            return this == GLOBAL ? context.globalScope : context.scope();
        }
    }
}
