package com.fiskmods.quantify.interpreter;

import com.fiskmods.quantify.exception.QtfException;
import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.jvm.FunctionAddress;
import com.fiskmods.quantify.library.QtfLibrary;
import com.fiskmods.quantify.member.MemberMap;
import com.fiskmods.quantify.member.MemberType;
import com.fiskmods.quantify.member.Scope;

import java.util.*;

import static com.fiskmods.quantify.exception.QtfParseException.error;

public class InterpreterState {
    private final Scope globalScope = new Scope();
    private final List<Scope> allScopes = new ArrayList<>();
    private final LinkedList<Scope> currentScope = new LinkedList<>();

    private final Map<String, Integer> inputs = new HashMap<>();
    private final String[] libraryKeys;
    private FunctionAddress[] functions = new FunctionAddress[8];

    private final InterpreterStack stack;

    public InterpreterState(InterpreterStack stack) {
        this.stack = stack;
        libraryKeys = new String[stack.parser.libraries()];
        currentScope.add(globalScope);
        allScopes.add(globalScope);
    }

    public MemberMap compile() throws QtfParseException {
        if (currentScope.size() > 1) {
            stack.reader.mark();
            throw QtfParseException.error(stack.reader, "unbalanced stack: " + currentScope.size());
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

    public boolean pop() {
        if (currentScope.size() <= 1) {
            return false;
        }
        allScopes.add(currentScope.removeLast());
        return true;
    }

    public void addInput(String name, int index) {
        inputs.put(name, index);
    }

    public int addMember(String name, MemberType type, ScopeLevel level) throws QtfParseException {
        try {
            return level.get(this).put(name, type);
        } catch (QtfException e) {
            throw error(stack.reader, e.getMessage());
        }
    }

    public int getMemberId(String name, MemberType expectedType, ScopeLevel level) throws QtfParseException {
        try {
            return level.get(this).get(name, expectedType);
        } catch (QtfException e) {
            throw error(stack.reader, e.getMessage());
        }
    }

    public boolean has(String name, MemberType expectedType, ScopeLevel level) {
        return level.get(this).has(name, expectedType);
    }

    public void addLibrary(String name, String key) throws QtfParseException {
        if (stack.parser.getLibrary(key) == null) {
            throw error(stack.reader, "unable to import library '%s', as it does not exist".formatted(key));
        }
        int id = addMember(name, MemberType.LIBRARY, ScopeLevel.GLOBAL);
        libraryKeys[id] = key;
    }

    public QtfLibrary getLibrary(int id) {
        return stack.parser.getLibrary(libraryKeys[id]);
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

    public int getLibraryFunction(int libId, String name) throws QtfParseException {
        QtfLibrary library = getLibrary(libId);
        String newName = libId + ":" + name;
        if (globalScope.has(newName, MemberType.FUNCTION)) {
            return getMemberId(newName, MemberType.FUNCTION, ScopeLevel.GLOBAL);
        }

        FunctionAddress function = library.getFunction(name);
        if (function == null) {
            throw error(stack.reader, "no such function '%s' in library '%s'"
                    .formatted(name, library.getKey()));
        }
        return addFunction(newName, function);
    }

    public String createTrace() {
        StringJoiner s = new StringJoiner("\n\t", "\t", "");
        List<String> libs = globalScope.getIdMap(MemberType.LIBRARY);
        if (!libs.isEmpty()) {
            StringJoiner sj = new StringJoiner(", ");
            for (int i = 0; i < libs.size(); ++i) {
                sj.add(i + " \"" + libraryKeys[i] + "\"" + " " + libs.get(i));
            }
            s.add("L: " + sj);
        }

        StringJoiner vars = new StringJoiner(", ");
        for (int i = 0; i < allScopes.size(); ++i) {
            List<String> v = allScopes.get(i).getIdMap(MemberType.VARIABLE);
            if (!v.isEmpty()) {
                vars.add(i + " " + v);
            }
        }
        if (vars.length() > 0) {
            s.add("V: " + vars);
        }

        List<String> list = globalScope.getIdMap(MemberType.FUNCTION);
        if (!list.isEmpty()) {
            StringJoiner sj = new StringJoiner(", ");
            list.forEach(sj::add);
            s.add("F: " + sj);
        }

        list = globalScope.getIdMap(MemberType.OUTPUT_VARIABLE);
        if (!list.isEmpty()) {
            StringJoiner sj = new StringJoiner(", ");
            list.forEach(sj::add);
            s.add("O: " + sj);
        }
        return s.toString();
    }

    public enum ScopeLevel {
        LOCAL, GLOBAL;
        public Scope get(InterpreterState state) {
            return this == GLOBAL ? state.globalScope : state.scope();
        }
    }
}
