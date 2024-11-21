package com.fiskmods.quantify.parser.element;

import com.fiskmods.quantify.lexer.Keywords;
import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.library.QtfLibrary;
import com.fiskmods.quantify.member.MemberType;
import com.fiskmods.quantify.parser.QtfParser;
import com.fiskmods.quantify.parser.SyntaxContext;
import com.fiskmods.quantify.parser.SyntaxElement;
import com.fiskmods.quantify.parser.SyntaxParser;

class NamespaceParser implements SyntaxParser<SyntaxElement> {
    public static final SyntaxParser<SyntaxElement> INSTANCE = new NamespaceParser();

    @Override
    public SyntaxElement accept(QtfParser parser, SyntaxContext context) throws QtfParseException {
        parser.next(TokenClass.NAMESPACE);
        String libName = parser.next(TokenClass.IDENTIFIER).getString();
        QtfLibrary namespace = null;

        if (!libName.equals(Keywords.THIS)) {
            int id = context.getMemberId(libName, MemberType.LIBRARY, SyntaxContext.ScopeLevel.LOCAL);
            namespace = context.getLibrary(id);
        }

        boolean skipped = parser.skip(TokenClass.TERMINATOR);
        if (!parser.isNext(TokenClass.OPEN_BRACES)) {
            context.scope().setNamespace(namespace);
            if (!skipped) {
                parser.next(TokenClass.TERMINATOR);
            }
            return null;
        }

        context.push();
        context.scope().setNamespace(namespace);
        StatementBody body = parser.next(StatementBody.PARSER);
        context.pop();
        return body;
    }
}
