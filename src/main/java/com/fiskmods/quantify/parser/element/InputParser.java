package com.fiskmods.quantify.parser.element;

import com.fiskmods.quantify.jvm.VariableType;
import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.member.MemberType;
import com.fiskmods.quantify.parser.QtfParser;
import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.parser.SyntaxContext;
import com.fiskmods.quantify.parser.SyntaxParser;

class InputParser implements SyntaxParser<Assignment> {
    public static final InputParser INSTANCE = new InputParser();

    @Override
    public Assignment accept(QtfParser parser, SyntaxContext context) throws QtfParseException {
        parser.clearPeekedToken();
        parser.next(TokenClass.OPEN_BRACKETS);
        int index = parser.next(TokenClass.NUM_LITERAL).getNumber().intValue();
        parser.next(TokenClass.CLOSE_BRACKETS);
        parser.next(TokenClass.COLON);

        String name = parser.next(TokenClass.IDENTIFIER).getString();
        int id = context.addMember(name, MemberType.VARIABLE, SyntaxContext.ScopeLevel.GLOBAL);
        context.addInput(name, index);
        parser.next(TokenClass.TERMINATOR);

        Variable var = new Variable(name, id, VariableType.LOCAL);
        Variable inputVar = new Variable(name, index, VariableType.INPUT);
        return new Assignment.AbsoluteAssignment(var, inputVar, null);
    }
}
