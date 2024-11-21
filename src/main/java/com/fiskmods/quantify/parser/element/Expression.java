package com.fiskmods.quantify.parser.element;

import com.fiskmods.quantify.lexer.token.Operator;
import com.fiskmods.quantify.lexer.token.Token;
import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.parser.QtfParser;
import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.parser.SyntaxContext;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

class Expression {
    private static Value accept(QtfParser parser, SyntaxContext context) throws QtfParseException {
        Token peeked = parser.peek();

        // Consumes any leading + or - signs
        if (peeked.type() == TokenClass.OPERATOR) {
            Operator op = peeked.getOperator();
            if (op == Operator.SUB) {
                parser.next();
                return accept(parser, context).negate();
            }
            if (op == Operator.ADD) {
                parser.next();
                return accept(parser, context);
            }
        }
        return switch (peeked.type()) {
            case IDENTIFIER -> parser.next(IdentifierParser.INSTANCE);
            case OPEN_PARENTHESIS -> {
                parser.clearPeekedToken();
                Value val = acceptEnclosed(parser, context);
                parser.next(TokenClass.CLOSE_PARENTHESIS);
                yield val;
            }
            default -> parser.next(NumLiteral.PARSER);
        };
    }

    public static Value acceptEnclosed(QtfParser parser, SyntaxContext context) throws QtfParseException {
        List<Object> stack = new ArrayList<>();
        Deque<Integer> lastPriority = new ArrayDeque<>();

        stack.add(parser.next(Expression::accept));

        while (parser.hasNext(QtfParser.Boundary.CLOSURE)) {
            Operator op = parser.next(TokenClass.OPERATOR).getOperator();
            Value right = parser.next(Expression::accept);

            while (!lastPriority.isEmpty() && lastPriority.peek() <= op.priority()) {
                reduce(stack);
                lastPriority.pop();
            }
            stack.add(op);
            stack.add(right);
            lastPriority.push(op.priority());
        }

        while (stack.size() > 1) {
            reduce(stack);
        }
        return (Value) stack.getFirst();
    }

    private static void reduce(List<Object> stack) {
        Value right = (Value) stack.removeLast();
        Operator op = (Operator) stack.removeLast();
        Value left = (Value) stack.removeLast();
        stack.add(Operation.wrap(left, right, op));
    }
}
