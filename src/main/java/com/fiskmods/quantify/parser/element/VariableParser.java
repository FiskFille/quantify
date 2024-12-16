package com.fiskmods.quantify.parser.element;

import com.fiskmods.quantify.exception.QtfException;
import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.jvm.VarAddress;
import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.member.MemberType;
import com.fiskmods.quantify.member.Struct;
import com.fiskmods.quantify.parser.SyntaxParser;

import java.util.Optional;

class VariableParser {
    static SyntaxParser<VarAddress> parser(boolean isDefinition) {
        return (parser, context) -> {
            String name = parser.next(TokenClass.IDENTIFIER).getString();
            return parser.next(parser(name, isDefinition));
        };
    }

    static SyntaxParser<VarAddress> parser(String name, boolean isDefinition) throws QtfParseException {
        return (parser, context) -> {
            try {
                // Definitions always belong to the default namespace
                if (isDefinition) {
                    return context.getDefaultNamespace().computeVariable(name, true);
                }

                String child = IdentifierParser.nextName(parser);
                if (child != null) {
                    Optional<Struct> struct = context.findMember(name, MemberType.STRUCT);

                    if (struct.isPresent()) {
                        child = StructRefParser.expandName(parser, child);
                        return struct.get().computeVariable(child, false);
                    }
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

    static SyntaxParser<VarAddress> parseOutput(String parentName, String name) {
        return (parser, context) -> {
            // Intentionally trigger exception if output doesn't exist
            context.getMember(parentName, MemberType.OUTPUT);

            StringBuilder nameBuilder = new StringBuilder(name);
            String next;
            while ((next = IdentifierParser.nextName(parser)) != null) {
                nameBuilder.append('.')
                        .append(next);
            }

            next = nameBuilder.toString();

            if (context.global().members.has(next, MemberType.VARIABLE)) {
                try {
                    return context.global().members.get(next, MemberType.VARIABLE);
                } catch (QtfException e) {
                    throw new QtfParseException(e);
                }
            } else {
                return context.addOutputVariable(next);
            }
        };
    }
}
