package com.fiskmods.quantify.parser.element;

import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.jvm.VariableType;
import com.fiskmods.quantify.lexer.Keywords;
import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.member.MemberType;
import com.fiskmods.quantify.member.Scope;
import com.fiskmods.quantify.parser.QtfParser;
import com.fiskmods.quantify.parser.SyntaxContext;
import com.fiskmods.quantify.parser.SyntaxElement;
import com.fiskmods.quantify.parser.SyntaxParser;
import org.objectweb.asm.MethodVisitor;

record InterpolateStatement(Value progress, VariableRef substitution, SyntaxElement body) implements SyntaxElement {
    public static final SyntaxParser<InterpolateStatement> PARSER = new InterpolateStatementParser();

    @Override
    public void apply(MethodVisitor mv) {
        if (progress instanceof NumLiteral(double value) && value == 0) {
            return;
        }
        if (substitution != null) {
            substitution.set(mv, progress, null);
        }
        body.apply(mv);
    }

    private static class InterpolateStatementParser implements SyntaxParser<InterpolateStatement> {
        @Override
        public InterpolateStatement accept(QtfParser parser, SyntaxContext context) throws QtfParseException {
            Value progress;
            VariableRef substitution;

            parser.next(TokenClass.INTERPOLATE);
            parser.next(TokenClass.OPEN_PARENTHESIS);
            progress = parser.next(ExpressionParser.INSTANCE);
            parser.next(TokenClass.CLOSE_PARENTHESIS);
            parser.skip(TokenClass.TERMINATOR);

            if (progress instanceof NumLiteral || progress instanceof VariableRef) {
                substitution = null;
            } else {
                // Store progress value in a variable if it's not a constant
                int id;
                if (context.hasMember(Keywords.INTERPOLATE, MemberType.VARIABLE)) {
                    id = context.getMemberId(Keywords.INTERPOLATE, MemberType.VARIABLE);
                } else {
                    id = context.addMember(Keywords.INTERPOLATE, MemberType.VARIABLE);
                }
                substitution = new VariableRef(id, VariableType.LOCAL);
            }

            StatementBody body = parser.next(new StatementBody.StatementBodyParser(t -> {
                Scope scope = t.copy();
                scope.setLerpProgress(substitution != null ? substitution : progress);
                return scope;
            }));
            return new InterpolateStatement(progress, substitution, body);
        }
    }
}
