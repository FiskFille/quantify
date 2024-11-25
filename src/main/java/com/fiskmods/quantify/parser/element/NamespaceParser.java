package com.fiskmods.quantify.parser.element;

import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.lexer.Keywords;
import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.member.Namespace;
import com.fiskmods.quantify.parser.QtfParser;
import com.fiskmods.quantify.parser.SyntaxContext;
import com.fiskmods.quantify.parser.SyntaxElement;
import com.fiskmods.quantify.parser.SyntaxParser;

class NamespaceParser implements SyntaxParser<SyntaxElement> {
    public static final SyntaxParser<SyntaxElement> INSTANCE = new NamespaceParser();

    @Override
    public SyntaxElement accept(QtfParser parser, SyntaxContext context) throws QtfParseException {
        parser.next(TokenClass.NAMESPACE);
        String namespaceName = parser.next(TokenClass.IDENTIFIER).getString();
        Namespace namespace;

        if (namespaceName.equals(Keywords.THIS)) {
            namespace = context.getDefaultNamespace();
        } else {
            namespace = Namespace.of(context.getLibrary(namespaceName));
        }

        boolean skipped = parser.skip(TokenClass.TERMINATOR);
        if (!parser.isNext(TokenClass.OPEN_BRACES)) {
            context.scope().setNamespace(namespace);
            if (!skipped) {
                parser.next(TokenClass.TERMINATOR);
            }
            return null;
        }
        return parser.next(StatementBody.parser(namespace));
    }
}
