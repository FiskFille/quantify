package com.fiskmods.quantify.parser.element;

import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.jvm.JvmFunction;
import com.fiskmods.quantify.jvm.VarAddress;
import com.fiskmods.quantify.jvm.assignable.NumVar;
import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.parser.QtfParser;
import com.fiskmods.quantify.parser.SyntaxContext;
import com.fiskmods.quantify.parser.SyntaxParser;

class PublicParser implements SyntaxParser<JvmFunction> {
    static final PublicParser INSTANCE = new PublicParser();

    @Override
    public JvmFunction accept(QtfParser parser, SyntaxContext context) throws QtfParseException {
        parser.clearPeekedToken();

        if (parser.isNext(TokenClass.VAR)) {
            parser.clearPeekedToken();
            String name = parser.next(TokenClass.IDENTIFIER).getString();
            VarAddress<NumVar> var = context.addPublicVar(name);

            if (parser.isNext(TokenClass.ASSIGNMENT, null)) {
                return parser.next(Assignment.parser(var, true));
            }
            return null;
        }

        parser.next(TokenClass.STRUCT);
        String name = parser.next(TokenClass.IDENTIFIER).getString();
        context.addPublicStruct(name);
        return null;
    }
}
