package com.fiskmods.quantify.parser.element;

import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.jvm.JvmFunction;
import com.fiskmods.quantify.jvm.VarAddress;
import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.jvm.assignable.Struct;
import com.fiskmods.quantify.parser.QtfParser;
import com.fiskmods.quantify.parser.SyntaxContext;
import com.fiskmods.quantify.parser.SyntaxParser;

class StructDefParser implements SyntaxParser<JvmFunction> {
    static final StructDefParser INSTANCE = new StructDefParser();

    @Override
    public JvmFunction accept(QtfParser parser, SyntaxContext context) throws QtfParseException {
        parser.clearPeekedToken();
        String name = parser.next(TokenClass.IDENTIFIER).getString();
        VarAddress<Struct> struct = context.addStruct(name);
        return struct::init;
    }
}
