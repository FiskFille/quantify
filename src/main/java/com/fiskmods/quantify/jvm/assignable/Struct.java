package com.fiskmods.quantify.jvm.assignable;

import com.fiskmods.quantify.exception.QtfException;
import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.jvm.FunctionAddress;
import com.fiskmods.quantify.jvm.JvmUtil;
import com.fiskmods.quantify.jvm.VarAddress;
import com.fiskmods.quantify.lexer.token.Operator;
import com.fiskmods.quantify.member.MemberMap;
import com.fiskmods.quantify.member.MemberType;
import com.fiskmods.quantify.member.Namespace;
import com.fiskmods.quantify.parser.element.Assignable;
import com.fiskmods.quantify.parser.element.Value;
import org.objectweb.asm.MethodVisitor;

import java.util.Optional;

import static org.objectweb.asm.Opcodes.*;

public abstract class Struct implements Namespace, Value, Assignable {
    @Override
    public void apply(MethodVisitor mv) {
    }

    @Override
    public void set(MethodVisitor mv, Value value) {
    }

    @Override
    public void modify(MethodVisitor mv, Value value, Operator op) {
    }

    @Override
    public void lerp(MethodVisitor mv, Value value, Value progress, boolean rotational) {
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
    public double getConstant(String name) {
        return 0;
    }

    @Override
    public boolean hasConstant(String name) {
        return false;
    }

    public void expand(String name) throws QtfParseException {
    }

    public static Struct create(int index) {
        return new Root(index);
    }

    public static VarAddress<Struct> createVar(int index) {
        return new VarAddress.Impl<>(VarType.STRUCT, create(index), false);
    }

    private static class Root extends Struct {
        private final MemberMap members = new MemberMap();
        private final int index;

        private int localIndexOffset;

        public Root(int index) {
            this.index = index;
        }

        @Override
        public void init(MethodVisitor mv) {
            JvmUtil.iconst(mv, localIndexOffset);
            mv.visitIntInsn(NEWARRAY, T_DOUBLE);
            mv.visitVarInsn(ASTORE, index);
        }

        @Override
        public void expand(String name) throws QtfParseException {
            try {
                Optional<MemberMap.Member<?>> member = members.find(name);
                if (member.isEmpty()) {
                    members.put(name, () -> new VarAddress.Impl<>(VarType.STRUCT, new Child(), false));
                } else {
                    member.get().cast(name, MemberType.VARIABLE)
                            .value().typeCheck(name, VarType.STRUCT);
                }
            } catch (QtfException e) {
                throw new QtfParseException(e);
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T extends Value & Assignable> VarAddress<T> computeVariable(
                VarType<T> type, String name, boolean isDefinition) throws QtfException {

            if (type == null || members.has(name)) {
                return members.get(name, MemberType.VARIABLE)
                        .cast(name, type);
            }

            if (type == VarType.STRUCT) {
                return (VarAddress<T>) members.put(name,
                        () -> new VarAddress.Impl<>(VarType.STRUCT, new Child(), false));
            }
            return (VarAddress<T>) members.put(name, () -> {
                VarAddress<NumVar> var = VarAddress.arrayAccess(index, localIndexOffset);
                ++localIndexOffset;
                return var;
            });
        }

        @Override
        public boolean hasVariable(String name) {
            Optional<MemberMap.Member<?>> member = members.find(name);
            return member.isEmpty() || member.get().type() == MemberType.VARIABLE;
        }

        private static class Child extends Struct {
            @Override
            public void init(MethodVisitor mv) {
            }

            @Override
            public <T extends Value & Assignable> VarAddress<T> computeVariable(
                    VarType<T> type, String name, boolean isDefinition) {
                return null;
            }

            @Override
            public boolean hasVariable(String name) {
                return false;
            }
        }
    }
}
