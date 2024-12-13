package com.fiskmods.quantify.parser.element;

import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.jvm.JvmFunction;
import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.member.Namespace;
import com.fiskmods.quantify.member.Scope;
import com.fiskmods.quantify.parser.QtfParser;
import com.fiskmods.quantify.parser.SyntaxContext;
import com.fiskmods.quantify.parser.SyntaxParser;
import com.fiskmods.quantify.parser.SyntaxTree;
import org.objectweb.asm.MethodVisitor;

import java.util.function.UnaryOperator;

record StatementBody(SyntaxTree tree) implements JvmFunction {
    public static final StatementBodyParser PARSER = new StatementBodyParser(Scope::copy);

    public static StatementBodyParser parser(Namespace namespace) {
        return new StatementBodyParser(t -> t.copy(namespace));
    }

    @Override
    public void apply(MethodVisitor mv) {
        tree.flatten().apply(mv);
    }

    record StatementBodyParser(UnaryOperator<Scope> scope) implements SyntaxParser<StatementBody> {
        @Override
        public StatementBody accept(QtfParser parser, SyntaxContext context) throws QtfParseException {
            parser.next(TokenClass.OPEN_BRACES);
            context.push(scope);

            SyntaxTree syntaxTree = new SyntaxTree(context);
            parser.clearPeekedToken();
            parser.parse(syntaxTree, true);

            context.pop();
            parser.next(TokenClass.CLOSE_BRACES);
            return new StatementBody(syntaxTree);
        }
    }
}
