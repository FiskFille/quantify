package com.fiskmods.quantify.member;

import com.fiskmods.quantify.exception.QtfException;

import java.util.*;

public class Scope {
    private final Map<String, MemberType> types = new HashMap<>();
    private final Map<MemberType, List<String>> ids = new EnumMap<>(MemberType.class);

    private int namespace = -1;

    public Scope copy() {
        Scope scope = new Scope();
        scope.types.putAll(types);
        scope.namespace = namespace;
        ids.forEach((k, v) -> scope.ids.put(k, new ArrayList<>(v)));
        return scope;
    }

    public Scope setNamespace(int namespace) {
        this.namespace = namespace;
        return this;
    }

    public int getNamespace() {
        return namespace;
    }

    public List<String> getIdMap(MemberType type) {
        List<String> list = ids.get(type);
        return list != null ? list : List.of();
    }

    public int put(String name, MemberType type) throws QtfException {
        if (types.put(name, type) != null) {
            throw new QtfException("duplicate member '%s'".formatted(name));
        }
        List<String> idList = ids.computeIfAbsent(type, k -> new ArrayList<>());
        idList.add(name);
        return idList.size() - 1;
    }

    public boolean has(String name, MemberType expectedType) {
        return types.get(name) == expectedType;
    }

    public int get(String name, MemberType expectedType) throws QtfException {
        MemberType foundType = types.get(name);
        if (foundType == null) {
            throw new QtfException("undefined %s '%s'".formatted(expectedType, name));
        }
        if (foundType != expectedType) {
            throw new QtfException("expected '%s' to be a %s, was %s"
                    .formatted(name, expectedType, foundType));
        }

        int id = ids.computeIfAbsent(expectedType, k -> new ArrayList<>())
                .indexOf(name);
        if (id == -1) {
            throw new QtfException("nonexistent %s id %s".formatted(expectedType, id));
        }
        return id;
    }
}
