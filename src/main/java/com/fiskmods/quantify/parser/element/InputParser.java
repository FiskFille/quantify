package com.fiskmods.quantify.parser.element;

import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.jvm.VariableType;
import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.member.MemberType;
import com.fiskmods.quantify.parser.QtfParser;
import com.fiskmods.quantify.parser.SyntaxContext;
import com.fiskmods.quantify.parser.SyntaxParser;

class InputParser implements SyntaxParser<Assignment> {
    public static final InputParser INSTANCE = new InputParser();

    @Override
    public Assignment accept(QtfParser parser, SyntaxContext context) throws QtfParseException {
        parser.clearPeekedToken();
        parser.next(TokenClass.OPEN_BRACKETS);
        int inputIndex = parser.next(TokenClass.NUM_LITERAL).getNumber().intValue();
        parser.next(TokenClass.CLOSE_BRACKETS);
        parser.next(TokenClass.COLON);

        String name = parser.next(TokenClass.IDENTIFIER).getString();
        int varId = context.addMember(name, MemberType.VARIABLE);
        context.addInput(name, inputIndex);
        parser.next(TokenClass.TERMINATOR);

        VariableRef var = new VariableRef(varId, VariableType.LOCAL);
        VariableRef inputVar = new VariableRef(inputIndex, VariableType.INPUT);
        return new Assignment.AbsoluteAssignment(var, inputVar, null);
    }
}
