package com.fiskmods.quantify.parser.element;

import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.lexer.token.Operator;
import com.fiskmods.quantify.lexer.token.Token;
import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.parser.QtfParser;
import com.fiskmods.quantify.parser.SyntaxContext;
import com.fiskmods.quantify.parser.SyntaxElement;
import com.fiskmods.quantify.parser.SyntaxParser;
import org.objectweb.asm.MethodVisitor;

interface Assignment extends SyntaxElement {
    record AssignmentParser(boolean isDefinition) implements SyntaxParser<Assignment> {
        @Override
        public Assignment accept(QtfParser parser, SyntaxContext context) throws QtfParseException {
            Assignable target = parser.next(new Assignable.AssignableParser(isDefinition));

            // Empty definition
            if (!parser.hasNext(QtfParser.Boundary.LINE)) {
                return new AbsoluteAssignment(target, null, null);
            }

            Token assignment = parser.next(TokenClass.ASSIGNMENT);
            Operator op = null;
            if (assignment.value() instanceof Operator) {
                if (isDefinition) {
                    throw QtfParseException.error("definitions can't use assignment operators", assignment);
                }
                op = (Operator) assignment.value();

                if ((op == Operator.LERP || op == Operator.LERP_ROT) &&
                        context.scope().getLerpProgress() == null) {
                    throw QtfParseException.error("interpolation assignments can only be used inside" +
                            " interpolate blocks", assignment);
                }
            }

            Value value = parser.next(ExpressionParser.INSTANCE);
            if (op == Operator.LERP || op == Operator.LERP_ROT) {
                return new LerpAssignment(target, value, context.scope().getLerpProgress(),
                        op == Operator.LERP_ROT);
            }
            return new AbsoluteAssignment(target, value, op);
        }
    }

    record AbsoluteAssignment(Assignable target, Value value, Operator op) implements Assignment {
        @Override
        public void apply(MethodVisitor mv) {
            if (value == null) {
                target.init(mv);
            } else {
                target.set(mv, value, op);
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
