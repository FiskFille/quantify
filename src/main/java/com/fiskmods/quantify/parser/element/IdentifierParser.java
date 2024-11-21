package com.fiskmods.quantify.parser.element;

import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.library.QtfLibrary;
import com.fiskmods.quantify.member.MemberType;
import com.fiskmods.quantify.parser.QtfParser;
import com.fiskmods.quantify.parser.SyntaxContext;
import com.fiskmods.quantify.parser.SyntaxParser;

class IdentifierParser implements SyntaxParser<Value> {
    public static final IdentifierParser INSTANCE = new IdentifierParser();

    @Override
    public Value accept(QtfParser parser, SyntaxContext context) throws QtfParseException {
        String name = parser.next(TokenClass.IDENTIFIER).getString();
        String next = nextName(parser);

        if (next == null) {
            QtfLibrary namespace = context.scope().getNamespace();
            if (namespace != null && namespace.hasFunction(name)) {
                return parser.next(FunctionRef.parser(name, namespace, true));
            }
            return parser.next(Variable.parseLocal(name, false));
        }
        if (context.has(name, MemberType.OUTPUT, SyntaxContext.ScopeLevel.GLOBAL)) {
            return parser.next(Variable.parseOutput(name, next));
        }

        int libId = context.getMemberId(name, MemberType.LIBRARY, SyntaxContext.ScopeLevel.GLOBAL);
        QtfLibrary library = context.getLibrary(libId);

        if (parser.isNext(TokenClass.OPEN_PARENTHESIS)) {
            return parser.next(FunctionRef.parser(next, library, true));
        }
        Double value = library.getConstant(next);
        if (value == null) {
            throw new QtfParseException("Unknown constant '%s' in library '%s'"
                    .formatted(next, library.getKey()));
        }
        return new NumLiteral(value);
    }

    private String nextName(QtfParser parser) throws QtfParseException {
        if (parser.isNext(TokenClass.DOT)) {
            parser.clearPeekedToken();
            return parser.next(TokenClass.IDENTIFIER).getString();
        }
        return null;
    }
}
