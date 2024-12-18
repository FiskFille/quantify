package com.fiskmods.quantify.parser.element;

import com.fiskmods.quantify.exception.QtfException;
import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.jvm.JvmFunction;
import com.fiskmods.quantify.jvm.VarAddress;
import com.fiskmods.quantify.jvm.assignable.Struct;
import com.fiskmods.quantify.jvm.assignable.VarType;
import com.fiskmods.quantify.lexer.Keywords;
import com.fiskmods.quantify.lexer.token.Token;
import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.library.QtfLibrary;
import com.fiskmods.quantify.member.MemberMap;
import com.fiskmods.quantify.member.MemberType;
import com.fiskmods.quantify.member.Namespace;
import com.fiskmods.quantify.parser.QtfParser;
import com.fiskmods.quantify.parser.SyntaxParser;

import java.util.Optional;
import java.util.function.BiFunction;

class IdentifierParser {
    static final SyntaxParser<?> LINE_START = from(IdentifierParser::lineStart);
    static final SyntaxParser<Value> ANY_VALUE = from(IdentifierParser::anyValue);

    static <T extends JvmFunction> SyntaxParser<T> from(BiFunction<String, Namespace, SyntaxParser<T>> nextParser) {
        return (parser, context) -> {
            Token token = parser.next(TokenClass.IDENTIFIER);
            String name = token.getString();

            if (!parser.isNext(TokenClass.DOT)) {
                return parser.next(nextParser.apply(name, context.namespace()));
            }
            parser.clearPeekedToken();
            String child = parser.next(TokenClass.IDENTIFIER).getString();
            if (Keywords.THIS.equals(name)) {
                return parser.next(nextParser.apply(child, context.getDefaultNamespace()));
            }

            Optional<MemberMap.Member<?>> member = context.findMember(name);
            if (member.isPresent()) {
                MemberType<?> type = member.get().type();

                if (type == MemberType.LIBRARY) {
                    Namespace namespace = Namespace.of((QtfLibrary) member.get().value());
                    return parser.next(nextParser.apply(child, namespace));
                }
                if (type == MemberType.VARIABLE && ((VarAddress<?>) member.get().value()).is(VarType.STRUCT)) {
                    Struct struct = (Struct) ((VarAddress<?>) member.get().value()).access();
                    if (parser.isNext(TokenClass.DOT)) {
                        child = expandName(parser, struct, child);
                    }
                    return parser.next(nextParser.apply(child, struct));
                }

                throw QtfParseException.error("expected '%s' to be a %s, was %s"
                        .formatted(name, MemberType.LIBRARY.name(), type.name()), token);
            }
            throw QtfParseException.error("undefined library '%s'".formatted(name), token);
        };
    }

    private static SyntaxParser<?> lineStart(String name, Namespace namespace) {
        return FunctionRef.tryParse(name, namespace, false)
                .or(Assignment.parserFrom(name, namespace));
    }

    @SuppressWarnings("unchecked")
    static SyntaxParser<Value> anyValue(String name, Namespace namespace) {
        return (SyntaxParser<Value>) FunctionRef.tryParse(name, namespace, true)
                .or((parser, context) -> {
                    try {
                        if (namespace.hasConstant(name)) {
                            return new NumLiteral(namespace.getConstant(name));
                        }
                        return namespace.computeVariable(VarType.NUM, name, false);
                    } catch (QtfException e) {
                        throw new QtfParseException(e);
                    }
                });
    }

    private static String nextName(QtfParser parser) throws QtfParseException {
        if (parser.isNext(TokenClass.DOT)) {
            parser.clearPeekedToken();
            return parser.next(TokenClass.IDENTIFIER).getString();
        }
        return null;
    }

    private static String expandName(QtfParser parser, Struct struct, String name) throws QtfParseException {
        StringBuilder b = new StringBuilder(name);
        while ((name = nextName(parser)) != null) {
            struct.expand(b.toString());
            b.append('.').append(name);
        }
        return b.toString();
    }
}
