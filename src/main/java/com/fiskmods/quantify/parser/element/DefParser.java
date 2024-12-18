package com.fiskmods.quantify.parser.element;

import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.jvm.JvmFunction;
import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.parser.QtfParser;
import com.fiskmods.quantify.parser.SyntaxContext;
import com.fiskmods.quantify.parser.SyntaxParser;

class DefParser implements SyntaxParser<JvmFunction> {
    static final DefParser INSTANCE = new DefParser();

    @Override
    public JvmFunction accept(QtfParser parser, SyntaxContext context) throws QtfParseException {
        parser.clearPeekedToken();
        String name = parser.next(TokenClass.IDENTIFIER).getString();

        if (parser.isNext(TokenClass.OPEN_PARENTHESIS)) {
            return parser.next(new FunctionDef.FunctionDefParser(name));
        }
        return parser.next(Assignment.parseDef(name));
    }
}
