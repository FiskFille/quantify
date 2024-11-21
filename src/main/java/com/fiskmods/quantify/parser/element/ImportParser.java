package com.fiskmods.quantify.parser.element;

import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.parser.*;

class ImportParser implements SyntaxParser<SyntaxElement> {
    public static final ImportParser INSTANCE = new ImportParser();

    @Override
    public SyntaxElement accept(QtfParser parser, SyntaxContext context) throws QtfParseException {
        parser.clearPeekedToken();
        String key = parser.next(TokenClass.STR_LITERAL).getString();
        parser.next(TokenClass.COLON);

        String name = parser.next(TokenClass.IDENTIFIER).getString();
        context.addLibrary(name, key);
        parser.next(TokenClass.TERMINATOR);
        return null;
    }
}
