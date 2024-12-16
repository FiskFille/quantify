package com.fiskmods.quantify.member;

import com.fiskmods.quantify.exception.QtfException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class MemberMap {
    private final Map<String, Member<?>> members = new HashMap<>();

    public void inherit(MemberMap other) {
        members.putAll(other.members);
    }

    public void inheritAllExcept(MemberMap other, MemberType<?> exceptType) {
        for (Map.Entry<String, Member<?>> e : other.members.entrySet()) {
            if (e.getValue().type() == exceptType) {
                continue;
            }
            members.put(e.getKey(), e.getValue());
        }
    }

    public Optional<Member<?>> find(String name) {
        return Optional.ofNullable(members.get(name));
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> find(String name, MemberType<T> expectedType) {
        Member<?> member = members.get(name);
        return member != null && member.type() == expectedType ?
                (Optional<T>) Optional.of(member.value()) : Optional.empty();
    }

    public boolean has(String name, MemberType<?> expectedType) {
        return find(name, expectedType).isPresent();
    }

    public <T> void put(String name, MemberType<T> type, T value) throws QtfException {
        if (members.containsKey(name)) {
            throw new QtfException("Duplicate member '%s'".formatted(name));
        }
        Member<T> member = new Member<>(type, value);
        members.put(name, member);
    }

    public <T> T put(String name, MemberType<T> type, Supplier<T> valueSupplier) throws QtfException {
        if (members.containsKey(name)) {
            throw new QtfException("Duplicate member '%s'".formatted(name));
        }
        T value = valueSupplier.get();
        Member<T> member = new Member<>(type, value);
        members.put(name, member);
        return value;
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String name, MemberType<T> expectedType) throws QtfException {
        Member<?> foundMember = members.get(name);
        if (foundMember == null) {
            throw new QtfException("Undefined %s '%s'".formatted(expectedType.name(), name));
        }
        if (foundMember.type() != expectedType) {
            throw new QtfException("Expected '%s' to be a %s, was %s"
                    .formatted(name, expectedType.name(), foundMember.type().name()));
        }
        return (T) foundMember.value();
    }

    public record Member<T>(MemberType<T> type, T value) {
    }
}
