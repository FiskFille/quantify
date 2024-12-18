package com.fiskmods.quantify.parser.element;

import com.fiskmods.quantify.jvm.JvmUtil;
import com.fiskmods.quantify.jvm.VarAddress;
import com.fiskmods.quantify.jvm.assignable.VarType;
import com.fiskmods.quantify.lexer.token.Operator;
import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.parser.SyntaxParser;
import org.objectweb.asm.MethodVisitor;

import java.util.ArrayList;
import java.util.List;

record VariableList<T extends Value & Assignable>(VarAddress<T>[] addresses) implements Assignable {
    @Override
    public void apply(MethodVisitor mv) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void set(MethodVisitor mv, Value value) {
        JvmUtil.set(mv, addresses, value);
    }

    @Override
    public void init(MethodVisitor mv) {
        for (VarAddress<?> address : addresses) {
            address.init(mv);
        }
    }

    @Override
    public void modify(MethodVisitor mv, Value value, Operator op) {
        JvmUtil.modify(mv, addresses, value, op);
    }

    @Override
    public void lerp(MethodVisitor mv, Value value, Value progress, boolean rotational) {
        JvmUtil.lerp(mv, addresses, progress, rotational, value);
    }

    VarType<T> type() {
        return addresses[0].type();
    }

    @SuppressWarnings("unchecked")
    static <T extends Value & Assignable> SyntaxParser<VariableList<T>> parse(
            VarAddress<T> firstVar, boolean isDefinition) {

        return (parser, context) -> {
            List<VarAddress<T>> list = new ArrayList<>();
            list.add(firstVar);
            do {
                parser.clearPeekedToken();
                list.add(Assignable.nextVariable(parser, firstVar.type(), isDefinition));
            } while (parser.isNext(TokenClass.COMMA));

            return new VariableList<>(list.toArray(new VarAddress[0]));
        };
    }
}
