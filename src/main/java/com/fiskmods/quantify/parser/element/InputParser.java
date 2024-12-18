package com.fiskmods.quantify.parser.element;

import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.jvm.VarAddress;
import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.parser.QtfParser;
import com.fiskmods.quantify.parser.SyntaxContext;
import com.fiskmods.quantify.parser.SyntaxParser;

class InputParser implements SyntaxParser<Assignment> {
    static final InputParser INSTANCE = new InputParser();

    @Override
    public Assignment accept(QtfParser parser, SyntaxContext context) throws QtfParseException {
        parser.clearPeekedToken();
        parser.next(TokenClass.OPEN_BRACKETS);
        int index = parser.next(TokenClass.NUM_LITERAL).getNumber().intValue();
        parser.next(TokenClass.CLOSE_BRACKETS);
        parser.next(TokenClass.COLON);

        String name = parser.next(TokenClass.IDENTIFIER).getString();
        VarAddress<?> var = context.addLocalVariable(name);
        VarAddress<?> inputVar = context.addInputVariable(name, index);

        parser.next(TokenClass.TERMINATOR);
        return new Assignment.AbsoluteAssignment(var, inputVar, null);
    }
}
