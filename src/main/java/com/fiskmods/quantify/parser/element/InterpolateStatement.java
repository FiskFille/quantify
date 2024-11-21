package com.fiskmods.quantify.parser.element;

import com.fiskmods.quantify.lexer.Keywords;
import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.jvm.VariableType;
import com.fiskmods.quantify.lexer.token.Token;
import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.member.MemberType;
import com.fiskmods.quantify.member.QtfMemory;
import com.fiskmods.quantify.parser.*;
import org.objectweb.asm.MethodVisitor;

record InterpolateStatement(Value progress, int progressId, SyntaxElement body) implements SyntaxElement {
    public static final SyntaxParser<InterpolateStatement> PARSER = new InterpolateStatementParser();

    @Override
    public void apply(MethodVisitor mv) {
        if (progress instanceof NumLiteral(double value) && value == 0) {
            return;
        }
        QtfMemory.set(progressId, VariableType.LOCAL, progress).apply(mv);
        body.apply(mv);
    }

    private static class InterpolateStatementParser implements SyntaxParser<InterpolateStatement> {
        @Override
        public InterpolateStatement accept(QtfParser parser, SyntaxContext context) throws QtfParseException {
            Token token = parser.next(TokenClass.INTERPOLATE);
            StatementBody body;
            int id;
            if (context.has(Keywords.INTERPOLATE, MemberType.VARIABLE, SyntaxContext.ScopeLevel.LOCAL)) {
                throw QtfParseException.error("interpolate statements cannot be nested", token);
            }

            parser.next(TokenClass.OPEN_PARENTHESIS);
            Value progress = parser.next(Expression::acceptEnclosed);
            parser.next(TokenClass.CLOSE_PARENTHESIS);
            parser.skip(TokenClass.TERMINATOR);

            context.push();
            id = context.addMember(Keywords.INTERPOLATE, MemberType.VARIABLE, SyntaxContext.ScopeLevel.LOCAL);
            body = parser.next(StatementBody.PARSER);
            context.pop();
            return new InterpolateStatement(progress, id, body);
        }
    }
}
