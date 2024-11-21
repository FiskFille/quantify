package com.fiskmods.quantify.parser.element;

import com.fiskmods.quantify.jvm.VariableType;
import com.fiskmods.quantify.lexer.token.Operator;
import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.member.MemberType;
import com.fiskmods.quantify.member.QtfMemory;
import com.fiskmods.quantify.parser.QtfParser;
import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.parser.SyntaxContext;
import com.fiskmods.quantify.parser.SyntaxParser;
import org.objectweb.asm.MethodVisitor;

record Variable(String name, int id, VariableType type) implements Value, Assignable {
    @Override
    public void apply(MethodVisitor mv) {
        QtfMemory.get(id, type).apply(mv);
    }

    @Override
    public void init(MethodVisitor mv) {
        QtfMemory.init(id).apply(mv);
    }

    @Override
    public void set(MethodVisitor mv, Value value, Operator op) {
        if (op != null) {
            QtfMemory.set(id, type, value, op).apply(mv);
        } else {
            QtfMemory.set(id, type, value).apply(mv);
        }
    }

    @Override
    public void lerp(MethodVisitor mv, Value value, int progressId, boolean rotational) {
        QtfMemory.lerp(new QtfMemory.Address(id, type, false),
                        QtfMemory.get(progressId, VariableType.LOCAL), rotational, value)
                .apply(mv);
    }

    public static SyntaxParser<Variable> parser(boolean isDefinition) {
        return (parser, context) -> {
            String name = parser.next(TokenClass.IDENTIFIER).getString();
            return parser.next(parser(name, isDefinition));
        };
    }

    public static SyntaxParser<Variable> parser(String name, boolean isDefinition) {
        if (isDefinition) {
            return parseLocal(name, true);
        }
        return (parser, context) -> {
            String child = nextName(parser);
            if (child != null) {
                return parser.next(parseOutput(name, child));
            }
            return parser.next(parseLocal(name, false));
        };
    }

    public static SyntaxParser<Variable> parseLocal(String name, boolean isDefinition) {
        return (parser, context) -> {
            int id;
            if (isDefinition) {
                id = context.addMember(name, MemberType.VARIABLE, SyntaxContext.ScopeLevel.LOCAL);
            } else {
                id = context.getMemberId(name, MemberType.VARIABLE, SyntaxContext.ScopeLevel.LOCAL);
            }
            return new Variable(name, id, VariableType.LOCAL);
        };
    }

    public static SyntaxParser<Variable> parseOutput(String parentName, String name) {
        return (parser, context) -> {
            context.getMemberId(parentName, MemberType.OUTPUT, SyntaxContext.ScopeLevel.GLOBAL);
            StringBuilder nameBuilder = new StringBuilder(name);
            String next;
            while ((next = nextName(parser)) != null) {
                nameBuilder.append('.')
                        .append(next);
            }

            next = nameBuilder.toString();
            int id;
            if (context.has(next, MemberType.OUTPUT_VARIABLE, SyntaxContext.ScopeLevel.GLOBAL)) {
                id = context.getMemberId(next, MemberType.OUTPUT_VARIABLE, SyntaxContext.ScopeLevel.GLOBAL);
            } else {
                id = context.addMember(next, MemberType.OUTPUT_VARIABLE, SyntaxContext.ScopeLevel.GLOBAL);
            }
            return new Variable(next, id, VariableType.OUTPUT);
        };
    }

    private static String nextName(QtfParser parser) throws QtfParseException {
        if (parser.isNext(TokenClass.DOT)) {
            parser.clearPeekedToken();
            return parser.next(TokenClass.IDENTIFIER).getString();
        }
        return null;
    }
}
