package com.fiskmods.quantify.parser.element;

import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.jvm.JvmFunction;
import com.fiskmods.quantify.jvm.JvmUtil;
import com.fiskmods.quantify.jvm.VarAddress;
import com.fiskmods.quantify.lexer.token.Operator;
import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.parser.QtfParser;
import com.fiskmods.quantify.parser.SyntaxContext;
import com.fiskmods.quantify.parser.SyntaxParser;
import org.objectweb.asm.MethodVisitor;

import java.util.ArrayList;
import java.util.List;

public interface Assignable extends JvmFunction {
    void set(MethodVisitor mv, Value value);

    default void init(MethodVisitor mv) {
        set(mv, Value.ZERO);
    }

    void modify(MethodVisitor mv, Value value, Operator op);

    void lerp(MethodVisitor mv, Value value, Value progress, boolean rotational);

    SyntaxParser<Assignable> PARSER = (parser, context) -> {
        String name = parser.next(TokenClass.IDENTIFIER).getString();
        if (parser.isNext(TokenClass.OPERATOR, Operator.SUB)) {
            parser.clearPeekedToken();
            return parser.next(new AssignableParser(name, true, false));
        }
        return parser.next(new AssignableParser(name, false, false));
    };

    record AssignableParser(String name, boolean isNegated, boolean isDefinition) implements SyntaxParser<Assignable> {
        @Override
        public Assignable accept(QtfParser parser, SyntaxContext context) throws QtfParseException {
            VariableRef var = parser.next(VariableRef.parser(name, isDefinition));
            if (!parser.isNext(TokenClass.COMMA)) {
                return isNegated ? new NegatedVariable(var) : var;
            }

            List<VarAddress> list = new ArrayList<>();
            list.add(VarAddress.create(var.type(), var.id(), isNegated));
            boolean isNegated;

            do {
                parser.clearPeekedToken();

                // Negated LHS variables are not allowed in assignments
                if (!isDefinition && parser.isNext(TokenClass.OPERATOR, Operator.SUB)) {
                    parser.clearPeekedToken();
                    isNegated = true;
                } else {
                    isNegated = false;
                }

                var = parser.next(VariableRef.parser(isDefinition));
                list.add(VarAddress.create(var.type(), var.id(), isNegated));
            } while (parser.isNext(TokenClass.COMMA));

            return new VariableList(list.toArray(new VarAddress[0]));
        }
    }

    record NegatedVariable(VariableRef var) implements Assignable {
        @Override
        public void apply(MethodVisitor mv) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void set(MethodVisitor mv, Value value) {
            var.set(mv, value.negate());
        }

        @Override
        public void modify(MethodVisitor mv, Value value, Operator op) {
            var.modify(mv, value.negate(), op);
        }

        @Override
        public void lerp(MethodVisitor mv, Value value, Value progress, boolean rotational) {
            var.lerp(mv, value.negate(), progress, rotational);
        }
    }

    record VariableList(VarAddress[] addresses) implements Assignable {
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
            for (VarAddress address : addresses) {
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
    }
}
