package com.fiskmods.quantify.parser.element;

import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.lexer.token.Token;
import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.library.QtfLibrary;
import com.fiskmods.quantify.member.MemberType;
import com.fiskmods.quantify.parser.QtfParser;
import com.fiskmods.quantify.parser.SyntaxContext;
import com.fiskmods.quantify.parser.SyntaxParser;

public class SyntaxSelector {
    public static SyntaxParser<?> selectSyntax(QtfParser parser, SyntaxContext context, Token next)
            throws QtfParseException {
        return switch (next.type()) {
            case IMPORT -> ImportParser.INSTANCE;
            case INPUT -> InputParser.INSTANCE;
            case OUTPUT -> OutputParser.INSTANCE;
            case IF -> IfStatement.PARSER;
            case INTERPOLATE -> InterpolateStatement.PARSER;
            case NAMESPACE -> NamespaceParser.INSTANCE;
            case IDENTIFIER -> selectIdentifierSyntax(parser, context, next);
            case DEF -> new Assignment.AssignmentParser(true);
            default -> new Assignment.AssignmentParser(false);
        };
    }

    private static SyntaxParser<?> selectIdentifierSyntax(QtfParser parser, SyntaxContext context, Token next)
            throws QtfParseException {
        String name = next.getString();
        QtfLibrary namespace = context.scope().getNamespace();

        if (namespace != null && namespace.hasFunction(name)) {
            return FunctionRef.parser(namespace, false);
        }
        if (context.has(name, MemberType.LIBRARY, SyntaxContext.ScopeLevel.GLOBAL)) {
            int id = context.getMemberId(name, MemberType.LIBRARY, SyntaxContext.ScopeLevel.GLOBAL);
            parser.clearPeekedToken();
            parser.next(TokenClass.DOT);
            return FunctionRef.parser(context.getLibrary(id), false);
        }
        return new Assignment.AssignmentParser(false);
    }
}
