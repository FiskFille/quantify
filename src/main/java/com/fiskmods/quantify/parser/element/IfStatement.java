package com.fiskmods.quantify.parser.element;

import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.parser.QtfParser;
import com.fiskmods.quantify.parser.SyntaxContext;
import com.fiskmods.quantify.parser.SyntaxElement;
import com.fiskmods.quantify.parser.SyntaxParser;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.*;

record IfStatement(Value condition, SyntaxElement body, SyntaxElement elseBody) implements SyntaxElement {
    public static final SyntaxParser<IfStatement> PARSER = new IfStatementParser();

    @Override
    public void apply(MethodVisitor mv) {
        if (condition instanceof NumLiteral(double value)) {
            if (value > 0) {
                body.apply(mv);
            }
            return;
        }

        Label end = new Label();
        condition.apply(mv);
        mv.visitInsn(D2I);

        if (elseBody == null) {
            mv.visitJumpInsn(IFLE, end);
            body.apply(mv);
        }
        else {
            Label els = new Label();
            mv.visitJumpInsn(IFLE, els);
            body.apply(mv);
            mv.visitJumpInsn(GOTO, end);
            mv.visitLabel(els);
            elseBody.apply(mv);
        }
        mv.visitLabel(end);
    }

    private static class IfStatementParser implements SyntaxParser<IfStatement> {
        @Override
        public IfStatement accept(QtfParser parser, SyntaxContext context) throws QtfParseException {
            parser.next(TokenClass.IF);
            parser.next(TokenClass.OPEN_PARENTHESIS);
            Value condition = parser.next(ExpressionParser.INSTANCE);
            parser.next(TokenClass.CLOSE_PARENTHESIS);
            parser.skip(TokenClass.TERMINATOR);

            StatementBody body = parser.next(StatementBody.PARSER);
            SyntaxElement elseBody = null;
            parser.skip(TokenClass.TERMINATOR);

            if (parser.isNext(TokenClass.ELSE)) {
                parser.clearPeekedToken();
                if (parser.isNext(TokenClass.IF)) {
                    elseBody = parser.next(this);
                }
                else {
                    elseBody = parser.next(StatementBody.PARSER);
                }
            }
            return new IfStatement(condition, body, elseBody);
        }
    }
}
