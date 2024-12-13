package com.fiskmods.quantify.parser.element;

import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.jvm.JvmFunction;
import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.member.MemberType;
import com.fiskmods.quantify.parser.QtfParser;
import com.fiskmods.quantify.parser.SyntaxContext;
import com.fiskmods.quantify.parser.SyntaxParser;

class OutputParser implements SyntaxParser<JvmFunction> {
    public static final OutputParser INSTANCE = new OutputParser();

    @Override
    public JvmFunction accept(QtfParser parser, SyntaxContext context) throws QtfParseException {
        parser.clearPeekedToken();
        parser.next(TokenClass.COLON);

        String name = parser.next(TokenClass.IDENTIFIER).getString();
        context.addMember(name, MemberType.OUTPUT);
        parser.next(TokenClass.TERMINATOR);
        return null;
    }
}
