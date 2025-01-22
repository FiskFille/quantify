package com.fiskmods.quantify.parser.element;

import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.jvm.JvmFunction;
import com.fiskmods.quantify.jvm.VarAddress;
import com.fiskmods.quantify.jvm.assignable.Struct;
import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.parser.QtfParser;
import com.fiskmods.quantify.parser.SyntaxContext;
import com.fiskmods.quantify.parser.SyntaxParser;

class StructDefParser implements SyntaxParser<VarAddress<Struct>> {
    static final SyntaxParser<VarAddress<Struct>> ADDRESS = new StructDefParser();
    static final SyntaxParser<JvmFunction> INSTANCE = ADDRESS.map(t -> t::init);

    @Override
    public VarAddress<Struct> accept(QtfParser parser, SyntaxContext context) throws QtfParseException {
        parser.clearPeekedToken();
        String name = parser.next(TokenClass.IDENTIFIER).getString();
        return context.addStruct(name);
    }
}
