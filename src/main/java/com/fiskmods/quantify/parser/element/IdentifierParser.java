package com.fiskmods.quantify.parser.element;

import com.fiskmods.quantify.exception.QtfException;
import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.lexer.Keywords;
import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.member.MemberMap;
import com.fiskmods.quantify.member.MemberType;
import com.fiskmods.quantify.member.Namespace;
import com.fiskmods.quantify.parser.QtfParser;
import com.fiskmods.quantify.parser.SyntaxContext;
import com.fiskmods.quantify.parser.SyntaxParser;

import java.util.Optional;

class IdentifierParser implements SyntaxParser<Value> {
    static final IdentifierParser INSTANCE = new IdentifierParser();

    @Override
    public Value accept(QtfParser parser, SyntaxContext context) throws QtfParseException {
        String name = parser.next(TokenClass.IDENTIFIER).getString();
        if (Keywords.THIS.equals(name)) {
            return accept(parser, context, context.getDefaultNamespace(), nextName(parser), false);
        }

        Optional<MemberMap.Member<?>> member = context.findMember(name);
        if (member.isPresent()) {
            MemberType<?> type = member.get().type();

            if (type == MemberType.OUTPUT) {
                return parser.next(VariableRef.parseOutput(name, nextName(parser)));
            }
            if (type == MemberType.LIBRARY) {
                return accept(parser, context,
                        Namespace.of(context.getMember(name, MemberType.LIBRARY)),
                        nextName(parser), false);
            }
        }
        return accept(parser, context, context.namespace(), name, true);
    }

    private Value accept(QtfParser parser, SyntaxContext context, Namespace namespace, String name,
                         boolean isImplicit) throws QtfParseException {
        try {
            if (namespace.hasFunction(name)) {
                return parser.next(FunctionRef.parser(namespace.getFunction(name), true));
            }
            if (namespace.hasConstant(name)) {
                return new NumLiteral(namespace.getConstant(name));
            }
            return namespace.computeVariable(name, false);
        } catch (QtfException e) {
            // If nothing was found in the library, search locally
            if (isImplicit && namespace != context.getDefaultNamespace()) {
                return accept(parser, context, context.getDefaultNamespace(), name, true);
            }
            throw new QtfParseException(e);
        }
    }

    private String nextName(QtfParser parser) throws QtfParseException {
        if (parser.isNext(TokenClass.DOT)) {
            parser.clearPeekedToken();
            return parser.next(TokenClass.IDENTIFIER).getString();
        }
        return null;
    }
}
