package com.fiskmods.quantify.parser.element;

import com.fiskmods.quantify.exception.QtfException;
import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.jvm.VarAddress;
import com.fiskmods.quantify.jvm.VariableType;
import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.member.MemberType;
import com.fiskmods.quantify.parser.QtfParser;
import com.fiskmods.quantify.parser.SyntaxParser;

public record VariableRef(int id, VariableType type) implements Value, VarAddress {
    @Override
    public boolean isNegated() {
        return false;
    }

    public static SyntaxParser<VariableRef> parser(boolean isDefinition) {
        return (parser, context) -> {
            String name = parser.next(TokenClass.IDENTIFIER).getString();
            return parser.next(parser(name, isDefinition));
        };
    }

    public static SyntaxParser<VariableRef> parser(String name, boolean isDefinition) throws QtfParseException {
        return (parser, context) -> {
            try {
                // Definitions always belong to the default namespace
                if (isDefinition) {
                    return context.getDefaultNamespace().computeVariable(name, true);
                }
                String child = nextName(parser);
                if (child != null) {
                    return parser.next(parseOutput(name, child));
                }

                if (context.namespace().hasVariable(name)) {
                    return context.namespace().computeVariable(name, false);
                }
                return context.getDefaultNamespace().computeVariable(name, false);
            } catch (QtfException e) {
                throw new QtfParseException(e);
            }
        };
    }

    public static SyntaxParser<VariableRef> parseOutput(String parentName, String name) {
        return (parser, context) -> {
            context.getMemberId(parentName, MemberType.OUTPUT);
            StringBuilder nameBuilder = new StringBuilder(name);
            String next;
            while ((next = nextName(parser)) != null) {
                nameBuilder.append('.')
                        .append(next);
            }

            next = nameBuilder.toString();
            int id;
            if (context.hasMember(next, MemberType.OUTPUT_VARIABLE)) {
                id = context.getMemberId(next, MemberType.OUTPUT_VARIABLE);
            } else {
                id = context.addMember(next, MemberType.OUTPUT_VARIABLE);
            }
            return new VariableRef(id, VariableType.OUTPUT);
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
