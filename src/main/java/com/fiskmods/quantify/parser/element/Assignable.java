package com.fiskmods.quantify.parser.element;

import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.jvm.VariableType;
import com.fiskmods.quantify.lexer.token.Operator;
import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.member.QtfMemory;
import com.fiskmods.quantify.parser.*;
import org.objectweb.asm.MethodVisitor;

import java.util.ArrayList;
import java.util.List;

interface Assignable extends SyntaxElement {
    void init(MethodVisitor mv);

    void set(MethodVisitor mv, Value value, Operator op);

    void lerp(MethodVisitor mv, Value value, int progressId, boolean rotational);

    record AssignableParser(boolean isDefinition) implements SyntaxParser<Assignable> {
        @Override
        public Assignable accept(QtfParser parser, SyntaxContext context) throws QtfParseException {
            boolean isNegated = false;

            if (isDefinition) {
                parser.next(TokenClass.DEF);
            } else if (parser.isNext(TokenClass.OPERATOR, Operator.SUB)) {
                parser.clearPeekedToken();
                isNegated = true;
            }

            Variable var = parser.next(Variable.parser(isDefinition));
            if (!parser.isNext(TokenClass.COMMA)) {
                return isNegated ? new NegatedVariable(var) : var;
            }

            List<QtfMemory.Address> list = new ArrayList<>();
            list.add(new QtfMemory.Address(var.id(), var.type(), isNegated));
            do {
                parser.clearPeekedToken();

                // Negated LHS variables are not allowed in assignments
                if (!isDefinition && parser.isNext(TokenClass.OPERATOR, Operator.SUB)) {
                    parser.clearPeekedToken();
                    isNegated = true;
                } else {
                    isNegated = false;
                }

                var = parser.next(Variable.parser(isDefinition));
                list.add(new QtfMemory.Address(var.id(), var.type(), isNegated));
            } while (parser.isNext(TokenClass.COMMA));

            return new VariableList(list.toArray(new QtfMemory.Address[0]));
        }
    }

    record NegatedVariable(Variable var) implements Assignable {
        @Override
        public void apply(MethodVisitor mv) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void init(MethodVisitor mv) {
            var.init(mv);
        }

        @Override
        public void set(MethodVisitor mv, Value value, Operator op) {
            var.set(mv, value.negate(), op);
        }

        @Override
        public void lerp(MethodVisitor mv, Value value, int progressId, boolean rotational) {
            var.lerp(mv, value.negate(), progressId, rotational);
        }
    }

    record VariableList(QtfMemory.Address[] addresses) implements Assignable {
        @Override
        public void apply(MethodVisitor mv) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void init(MethodVisitor mv) {
            for (QtfMemory.Address address : addresses) {
                QtfMemory.init(address.id()).apply(mv);
            }
        }

        @Override
        public void set(MethodVisitor mv, Value value, Operator op) {
            if (op != null) {
                QtfMemory.set(addresses, value, op).apply(mv);
            } else {
                QtfMemory.set(addresses, value).apply(mv);
            }
        }

        @Override
        public void lerp(MethodVisitor mv, Value value, int progressId, boolean rotational) {
            QtfMemory.lerp(addresses, QtfMemory.get(progressId, VariableType.LOCAL), rotational, value)
                    .apply(mv);
        }
    }
}
