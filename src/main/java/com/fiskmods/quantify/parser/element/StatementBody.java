package com.fiskmods.quantify.parser.element;

import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.parser.*;
import org.objectweb.asm.MethodVisitor;

record StatementBody(SyntaxTree tree) implements SyntaxElement {
    public static final SyntaxParser<StatementBody> PARSER = new StatementBodyParser();

    @Override
    public void apply(MethodVisitor mv) {
        tree.flatten().apply(mv);
    }

    private static class StatementBodyParser implements SyntaxParser<StatementBody> {
        @Override
        public StatementBody accept(QtfParser parser, SyntaxContext context) throws QtfParseException {
            parser.next(TokenClass.OPEN_BRACES);
            context.push();

            SyntaxTree syntaxTree = new SyntaxTree(context);
            parser.clearPeekedToken();
            parser.parse(syntaxTree, true);

            context.pop();
            parser.next(TokenClass.CLOSE_BRACES);
            return new StatementBody(syntaxTree);
        }
    }
}
