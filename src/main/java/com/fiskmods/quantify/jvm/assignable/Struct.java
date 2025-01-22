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

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

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
        return new RootStruct(index, new AtomicInteger());
    }

    public static Struct create(String name, int index, AtomicInteger localIndex, List<String> outputs) {
        return new PublicStruct(name, index, localIndex, outputs);
    }

    public static VarAddress<Struct> createVar(int index) {
        return VarAddress.create(VarType.STRUCT, create(index), false);
    }

    private static class RootStruct extends Struct {
        private final MemberMap members = new MemberMap();
        private final int index;

        private final AtomicInteger localIndex;

        public RootStruct(int index, AtomicInteger localIndex) {
            this.index = index;
            this.localIndex = localIndex;
        }

        @Override
        public void init(MethodVisitor mv) {
            JvmUtil.iconst(mv, localIndex.get());
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

            VarAddress<T> var;
            if (type == VarType.STRUCT) {
                var = (VarAddress<T>) members.put(name,
                        () -> VarAddress.create(VarType.STRUCT, new Child(), false));
            } else {
                var = (VarAddress<T>) members.put(name,
                        () -> VarAddress.arrayAccess(index, localIndex.getAndIncrement()));
            }
            onVariableAdded(name, var);
            return var;
        }

        public <T extends Value & Assignable> void onVariableAdded(String name, VarAddress<T> var) {
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

    private static class PublicStruct extends RootStruct {
        private final String rootName;
        private final List<String> outputs;

        public PublicStruct(String name, int index, AtomicInteger localIndex, List<String> outputs) {
            super(index, localIndex);
            this.rootName = name;
            this.outputs = outputs;
        }

        @Override
        public <T extends Value & Assignable> void onVariableAdded(String name, VarAddress<T> var) {
            if (var.type() == VarType.NUM) {
                outputs.add(rootName + '.' + name);
            }
        }
    }
}
