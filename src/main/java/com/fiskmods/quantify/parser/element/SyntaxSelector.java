package com.fiskmods.quantify.parser.element;

import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.lexer.token.Token;
import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.library.QtfLibrary;
import com.fiskmods.quantify.member.MemberType;
import com.fiskmods.quantify.member.Namespace;
import com.fiskmods.quantify.parser.QtfParser;
import com.fiskmods.quantify.parser.SyntaxContext;
import com.fiskmods.quantify.parser.SyntaxParser;

public class SyntaxSelector {
    public static SyntaxParser<?> selectSyntax(QtfParser parser, SyntaxContext context, Token next)
            throws QtfParseException {
        return switch (next.type()) {
            case IMPORT -> checkScope(ImportParser.INSTANCE, context, next);
            case INPUT -> checkScope(InputParser.INSTANCE, context, next);
            case OUTPUT -> checkScope(OutputParser.INSTANCE, context, next);
            case IF -> IfStatement.PARSER;
            case INTERPOLATE -> InterpolateStatement.PARSER;
            case NAMESPACE -> NamespaceParser.INSTANCE;
            case DEF -> DefParser.INSTANCE;
            case CONST -> ConstDefParser.INSTANCE;
            case RETURN -> Return.INSTANCE;

            case IDENTIFIER -> selectIdentifierSyntax(parser, context, next);
            default -> Assignment.PARSER;
        };
    }

    private static SyntaxParser<?> checkScope(SyntaxParser<?> syntaxParser, SyntaxContext context, Token next)
            throws QtfParseException {
        if (context.scope().isInnerScope()) {
            throw new QtfParseException("Illegal token '" + next + "'",
                    "unavailable in inner scopes", next);
        }
        return syntaxParser;
    }

    private static SyntaxParser<?> selectIdentifierSyntax(QtfParser parser, SyntaxContext context, Token next)
            throws QtfParseException {
        String name = next.getString();
        MemberType memberType = context.typeOf(name);

        if (memberType == MemberType.LIBRARY) {
            QtfLibrary library = context.getLibrary(name);
            parser.clearPeekedToken();
            parser.next(TokenClass.DOT);
            return FunctionRef.parser(Namespace.of(library), false);
        }
        if (context.namespace().hasFunction(name)) {
            return FunctionRef.parser(context.namespace(), false);
        }
        return Assignment.PARSER;
    }
}
