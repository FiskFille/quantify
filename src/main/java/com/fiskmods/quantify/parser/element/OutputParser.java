package com.fiskmods.quantify.parser.element;

import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.member.MemberType;
import com.fiskmods.quantify.parser.*;

class OutputParser implements SyntaxParser<SyntaxElement> {
    public static final OutputParser INSTANCE = new OutputParser();

    @Override
    public SyntaxElement accept(QtfParser parser, SyntaxContext context) throws QtfParseException {
        parser.clearPeekedToken();
        parser.next(TokenClass.COLON);

        String name = parser.next(TokenClass.IDENTIFIER).getString();
        context.addMember(name, MemberType.OUTPUT, SyntaxContext.ScopeLevel.GLOBAL);
        parser.next(TokenClass.TERMINATOR);
        return null;
    }
}
