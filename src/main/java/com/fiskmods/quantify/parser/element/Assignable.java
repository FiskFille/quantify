package com.fiskmods.quantify.parser.element;

import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.jvm.JvmFunction;
import com.fiskmods.quantify.jvm.JvmUtil;
import com.fiskmods.quantify.jvm.VarAddress;
import com.fiskmods.quantify.lexer.token.Operator;
import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.parser.QtfParser;
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

    static SyntaxParser<Assignable> parse(boolean isDefinition) {
        return (parser, context) -> {
            VarAddress var = nextVariable(parser, isDefinition);
            return parser.next(parseList(var, isDefinition));
        };
    }

    static SyntaxParser<Assignable> parse(String name, boolean isDefinition) {
        return (parser, context) -> {
            VarAddress var = parser.next(VariableParser.parser(name, isDefinition));
            return parser.next(parseList(var, isDefinition));
        };
    }

    static SyntaxParser<Assignable> parseList(VarAddress firstVar, boolean isDefinition) {
        return (parser, context) -> {
            if (!parser.isNext(TokenClass.COMMA)) {
                return firstVar;
            }

            List<VarAddress> list = new ArrayList<>();
            list.add(firstVar);
            do {
                parser.clearPeekedToken();
                list.add(nextVariable(parser, isDefinition));
            } while (parser.isNext(TokenClass.COMMA));

            return new VariableList(list.toArray(new VarAddress[0]));
        };
    }

    private static VarAddress nextVariable(QtfParser parser, boolean isDefinition) throws QtfParseException {
        boolean isNegated = isNegated(parser, isDefinition);
        VarAddress var = parser.next(VariableParser.parser(isDefinition));
        if (isNegated) {
            return VarAddress.create(var.access(), true);
        }
        return var;
    }

    private static boolean isNegated(QtfParser parser, boolean isDefinition) {
        // Negated LHS variables are not allowed in assignments
        if (!isDefinition && parser.isNext(TokenClass.OPERATOR, Operator.SUB)) {
            parser.clearPeekedToken();
            return true;
        }
        return false;
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
