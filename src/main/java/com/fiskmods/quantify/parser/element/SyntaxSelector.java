package com.fiskmods.quantify.parser.element;

import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.lexer.token.Token;
import com.fiskmods.quantify.parser.SyntaxContext;
import com.fiskmods.quantify.parser.SyntaxParser;

public class SyntaxSelector {
    public static SyntaxParser<?> selectSyntax(SyntaxContext context, Token next) throws QtfParseException {
        return switch (next.type()) {
            case IMPORT -> checkScope(ImportParser.INSTANCE, context, next);
            case INPUT -> checkScope(InputParser.INSTANCE, context, next);
            case OUTPUT -> checkScope(OutputParser.INSTANCE, context, next);
            case IF -> IfStatement.PARSER;
            case INTERPOLATE -> InterpolateStatement.PARSER;
            case NAMESPACE -> NamespaceParser.INSTANCE;
            case DEF -> DefParser.INSTANCE;
            case CONST -> ConstDefParser.INSTANCE;
            case STRUCT -> StructDefParser.INSTANCE;
            case RETURN -> Return.INSTANCE;

            default -> IdentifierParser.LINE_START;
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
}
