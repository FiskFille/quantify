package com.fiskmods.quantify.member;

import com.fiskmods.quantify.exception.QtfException;
import com.fiskmods.quantify.parser.element.Value;

import java.util.*;

public class Scope {
    protected final Map<String, MemberType> types = new HashMap<>();
    protected final Map<MemberType, List<String>> ids = new EnumMap<>(MemberType.class);

    protected Namespace namespace;
    protected Value lerpProgress;

    public Scope(Namespace namespace) {
        this.namespace = namespace;
    }

    public Scope copy(Namespace namespace) {
        Scope scope = new Scope(namespace);
        scope.lerpProgress = lerpProgress;
        scope.types.putAll(types);
        ids.forEach((k, v) -> scope.ids.put(k, new ArrayList<>(v)));
        return scope;
    }

    public Scope copy() {
        return copy(namespace);
    }

    public void setNamespace(Namespace namespace) {
        this.namespace = namespace;
    }

    public Namespace getNamespace() {
        return namespace;
    }

    public void setLerpProgress(Value lerpProgress) {
        this.lerpProgress = lerpProgress;
    }

    public Value getLerpProgress() {
        return lerpProgress;
    }

    public List<String> getIdMap(MemberType type) {
        List<String> list = ids.get(type);
        return list != null ? list : List.of();
    }

    public int put(String name, MemberType type) throws QtfException {
        if (types.put(name, type) != null) {
            throw new QtfException("Duplicate member '%s'".formatted(name));
        }
        List<String> idList = ids.computeIfAbsent(type, k -> new ArrayList<>());
        idList.add(name);
        return idList.size() - 1;
    }

    public MemberType getType(String name) {
        return types.getOrDefault(name, MemberType.UNKNOWN);
    }

    public boolean has(String name, MemberType expectedType) {
        return types.get(name) == expectedType;
    }

    public int get(String name, MemberType expectedType) throws QtfException {
        MemberType foundType = types.get(name);
        if (foundType == null) {
            throw new QtfException("Undefined %s '%s'".formatted(expectedType, name));
        }
        if (foundType != expectedType) {
            throw new QtfException("Expected '%s' to be a %s, was %s"
                    .formatted(name, expectedType, foundType));
        }

        int id = ids.computeIfAbsent(expectedType, k -> new ArrayList<>())
                .indexOf(name);
        if (id == -1) {
            throw new QtfException("Nonexistent %s id %d".formatted(expectedType, id));
        }
        return id;
    }

    public boolean isParameter(String name) {
        return false;
    }
}
