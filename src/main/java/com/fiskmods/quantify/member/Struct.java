package com.fiskmods.quantify.member;

import com.fiskmods.quantify.exception.QtfException;
import com.fiskmods.quantify.jvm.FunctionAddress;
import com.fiskmods.quantify.jvm.JvmFunction;
import com.fiskmods.quantify.jvm.JvmUtil;
import com.fiskmods.quantify.jvm.VarAddress;
import org.objectweb.asm.MethodVisitor;

import java.util.Optional;

import static org.objectweb.asm.Opcodes.*;

public abstract class Struct implements JvmFunction, Namespace {
    public abstract void init(MethodVisitor mv);

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

    public static Struct create(int index) {
        return new Root(index);
    }

    private static class Root extends Struct {
        private final MemberMap members = new MemberMap();
        private final int index;

        private int localIndexOffset;

        public Root(int index) {
            this.index = index;
        }

        @Override
        public void apply(MethodVisitor mv) {
        }

        @Override
        public void init(MethodVisitor mv) {
            JvmUtil.iconst(mv, localIndexOffset);
            mv.visitIntInsn(NEWARRAY, T_DOUBLE);
            mv.visitVarInsn(ASTORE, index);
        }

        @Override
        public VarAddress computeVariable(String name, boolean isDefinition) throws QtfException {
            try {
                if (members.has(name, MemberType.VARIABLE)) {
                    return members.get(name, MemberType.VARIABLE);
                }
                return members.<VarAddress>put(name, MemberType.VARIABLE, () -> {
                    VarAddress var = VarAddress.arrayAccess(index, localIndexOffset);
                    ++localIndexOffset;
                    return var;
                });
            } finally {
                for (int i = 0; i < name.length(); ++i) {
                    if (name.charAt(i) != '.') {
                        continue;
                    }
                    String subName = name.substring(0, i);
                    Optional<MemberMap.Member<?>> member = members.find(subName);

                    if (member.isEmpty()) {
                        members.<Struct> put(subName, MemberType.STRUCT, Child::new);
                    } else {
                        member.get().typeCheck(subName, MemberType.STRUCT);
                    }
                }
            }
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
            public void apply(MethodVisitor mv) {
            }

            @Override
            public VarAddress computeVariable(String name, boolean isDefinition) {
                return null;
            }

            @Override
            public boolean hasVariable(String name) {
                return false;
            }
        }
    }
}
