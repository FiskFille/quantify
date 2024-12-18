package com.fiskmods.quantify.member;

import com.fiskmods.quantify.exception.QtfException;
import com.fiskmods.quantify.jvm.VarAddress;
import com.fiskmods.quantify.parser.element.Assignable;
import com.fiskmods.quantify.parser.element.Value;

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

    public boolean has(String name) {
        return find(name).isPresent();
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
    public <T extends Value & Assignable> VarAddress<T> put(String name, Supplier<VarAddress<T>> valueSupplier)
            throws QtfException {
        return (VarAddress<T>) this.<VarAddress<?>> put(name, MemberType.VARIABLE, valueSupplier::get);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String name, MemberType<T> expectedType) throws QtfException {
        Member<?> foundMember = members.get(name);
        if (foundMember == null) {
            throw new QtfException("Undefined %s '%s'".formatted(expectedType.name(), name));
        }
        foundMember.typeCheck(name, expectedType);
        return (T) foundMember.value();
    }

    public record Member<T>(MemberType<T> type, T value) {
        public void typeCheck(String name, MemberType<?> expectedType) throws QtfException {
            if (type != expectedType) {
                throw new QtfException("Expected '%s' to be a %s, was %s"
                        .formatted(name, expectedType.name(), type.name()));
            }
        }

        @SuppressWarnings("unchecked")
        public <U> Member<U> cast(String name, MemberType<U> expectedType) throws QtfException {
            typeCheck(name, expectedType);
            return (Member<U>) this;
        }
    }
}
