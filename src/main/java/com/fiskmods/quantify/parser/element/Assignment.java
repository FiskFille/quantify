package com.fiskmods.quantify.parser.element;

import com.fiskmods.quantify.jvm.JvmFunction;
import com.fiskmods.quantify.jvm.VarAddress;
import com.fiskmods.quantify.jvm.assignable.VarType;
import com.fiskmods.quantify.lexer.token.Operator;
import com.fiskmods.quantify.lexer.token.Token;
import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.member.Namespace;
import com.fiskmods.quantify.parser.QtfParser;
import com.fiskmods.quantify.parser.SyntaxParser;
import org.objectweb.asm.MethodVisitor;

interface Assignment extends JvmFunction {
    static SyntaxParser<Assignment> parser(Assignable target, boolean isDefinition) {
        return (parser, context) -> {
            // Empty definition
            if (!parser.hasNext(QtfParser.Boundary.LINE)) {
                return new Assignment.AbsoluteAssignment(target, null, null);
            }

            Token assignment = parser.next(TokenClass.ASSIGNMENT);
            Operator op = assignment.getAssignmentOperator(context, isDefinition);

            Value value = parser.next(ExpressionParser.INSTANCE);
            if (op == Operator.LERP || op == Operator.LERP_ROT) {
                return new Assignment.LerpAssignment(target, value, context.scope().getLerpProgress(),
                        op == Operator.LERP_ROT);
            }
            return new Assignment.AbsoluteAssignment(target, value, op);
        };
    }

    static SyntaxParser<Assignment> parserFrom(String name, Namespace namespace) {
        return (parser, context) -> {
            VarAddress<?> firstVar = VariableParser.compute(name, namespace, VarType.NUM, false);

            if (parser.isNext(TokenClass.COMMA)) {
                VariableList<?> list = parser.next(VariableList.parse(firstVar, false));
                return parser.next(parser(list, false));
            }
            return parser.next(parser(firstVar, false));
        };
    }

    static SyntaxParser<Assignment> parseDef(String name) {
        return VariableParser.def(name, VarType.NUM)
                .sequence(var -> Assignable.parse(var, true))
                .sequence(target -> Assignment.parser(target, true));
    }

    record AbsoluteAssignment(Assignable target, Value value, Operator op) implements Assignment {
        @Override
        public void apply(MethodVisitor mv) {
            if (value == null) {
                target.init(mv);
            } else if (op != null) {
                target.modify(mv, value, op);
            } else {
                target.set(mv, value);
            }
        }
    }

    record LerpAssignment(Assignable target, Value value, Value progress, boolean rotational) implements Assignment {
        @Override
        public void apply(MethodVisitor mv) {
            target.lerp(mv, value, progress, rotational);
        }
    }
}
