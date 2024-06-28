package com.fiskmods.quantify.member;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class OutputTree {
    private final Map<String, Variable> vars = new HashMap<>();

    private OutputTree() {
    }

    public static OutputTree create() {
        return new OutputTree();
    }

    public Set<String> keys() {
        return Collections.unmodifiableSet(vars.keySet());
    }

    public boolean contains(String key) {
        return vars.containsKey(key);
    }

    public Variable get(String key) {
        return vars.get(key);
    }

    public void resolve(QtfListener.Resolver resolver, QtfListener.Output output) {
        if (!vars.isEmpty()) {
            throw new IllegalStateException("Output tree has already been resolved!");
        }
        output.keys().forEach(t -> {
            Variable var = Variable.create();
            resolver.subscribe(var, t);
            vars.put(t, var);
        });
    }
}
