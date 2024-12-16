package com.fiskmods.quantify.parser.element;

import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.jvm.JvmFunction;
import com.fiskmods.quantify.jvm.VarAddress;
import com.fiskmods.quantify.lexer.Keywords;
import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.member.MemberType;
import com.fiskmods.quantify.member.Scope;
import com.fiskmods.quantify.parser.QtfParser;
import com.fiskmods.quantify.parser.SyntaxContext;
import com.fiskmods.quantify.parser.SyntaxParser;
import org.objectweb.asm.MethodVisitor;

record InterpolateStatement(Value progress, VarAddress substitution, JvmFunction body) implements JvmFunction {
    static final SyntaxParser<InterpolateStatement> PARSER = new InterpolateStatementParser();

    @Override
    public void apply(MethodVisitor mv) {
        if (progress instanceof NumLiteral(double value) && value == 0) {
            return;
        }
        if (substitution != null) {
            substitution.modify(mv, progress, null);
        }
        body.apply(mv);
    }

    private static class InterpolateStatementParser implements SyntaxParser<InterpolateStatement> {
        @Override
        public InterpolateStatement accept(QtfParser parser, SyntaxContext context) throws QtfParseException {
            Value progress;
            VarAddress substitution;

            parser.next(TokenClass.INTERPOLATE);
            parser.next(TokenClass.OPEN_PARENTHESIS);
            progress = parser.next(ExpressionParser.INSTANCE);
            parser.next(TokenClass.CLOSE_PARENTHESIS);
            parser.skip(TokenClass.TERMINATOR);

            if (progress instanceof NumLiteral || progress instanceof VarAddress) {
                substitution = null;
            } else {
                // Store progress value in a variable if it's not a constant
                if (context.hasMember(Keywords.INTERPOLATE, MemberType.VARIABLE)) {
                    substitution = context.getMember(Keywords.INTERPOLATE, MemberType.VARIABLE);
                } else {
                    substitution = context.addLocalVariable(Keywords.INTERPOLATE);
                }
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
