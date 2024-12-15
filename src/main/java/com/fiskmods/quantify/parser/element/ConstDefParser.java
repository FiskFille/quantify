package com.fiskmods.quantify.parser.element;

import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.jvm.JvmFunction;
import com.fiskmods.quantify.lexer.token.Operator;
import com.fiskmods.quantify.lexer.token.Token;
import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.member.MemberType;
import com.fiskmods.quantify.parser.QtfParser;
import com.fiskmods.quantify.parser.SyntaxContext;
import com.fiskmods.quantify.parser.SyntaxParser;

class ConstDefParser implements SyntaxParser<JvmFunction> {
    static final ConstDefParser INSTANCE = new ConstDefParser();

    @Override
    public JvmFunction accept(QtfParser parser, SyntaxContext context) throws QtfParseException {
        parser.clearPeekedToken();
        String name = parser.next(TokenClass.IDENTIFIER).getString();
        Token assignment = parser.next(TokenClass.ASSIGNMENT);

        if (assignment.value() instanceof Operator) {
            throw QtfParseException.error("definitions can't use assignment operators", assignment);
        }
        Value value = parser.next(ExpressionParser.INSTANCE);
        if (value instanceof NumLiteral(double v)) {
            context.addMember(name, MemberType.CONSTANT, v);
            return null;
        }
        throw QtfParseException.error("constants can't be assigned to variables or functions", assignment);
    }
}
