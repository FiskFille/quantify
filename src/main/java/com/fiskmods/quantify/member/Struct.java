package com.fiskmods.quantify.member;

import com.fiskmods.quantify.exception.QtfException;
import com.fiskmods.quantify.jvm.FunctionAddress;
import com.fiskmods.quantify.jvm.JvmFunction;
import com.fiskmods.quantify.jvm.JvmUtil;
import com.fiskmods.quantify.jvm.VarAddress;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.*;

public class Struct implements JvmFunction, Namespace {
    private final MemberMap members = new MemberMap();
    private final int index;

    private int localIndexOffset;

    public Struct(int index) {
        this.index = index;
    }

    @Override
    public void apply(MethodVisitor mv) {
    }

    public void init(MethodVisitor mv) {
        JvmUtil.iconst(mv, localIndexOffset);
        mv.visitIntInsn(NEWARRAY, T_DOUBLE);
        mv.visitVarInsn(ASTORE, index);
    }

    @Override
    public VarAddress computeVariable(String name, boolean isDefinition) throws QtfException {
        if (members.has(name, MemberType.VARIABLE)) {
            return members.get(name, MemberType.VARIABLE);
        }
        return members.<VarAddress> put(name, MemberType.VARIABLE, () -> {
            VarAddress var = VarAddress.arrayAccess(index, localIndexOffset);
            ++localIndexOffset;
            return var;
        });
    }

    @Override
    public boolean hasVariable(String name) {
        return true;
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
}
