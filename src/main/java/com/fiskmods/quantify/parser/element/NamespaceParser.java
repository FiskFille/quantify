package com.fiskmods.quantify.parser.element;

import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.jvm.JvmFunction;
import com.fiskmods.quantify.lexer.Keywords;
import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.member.MemberType;
import com.fiskmods.quantify.member.Namespace;
import com.fiskmods.quantify.parser.QtfParser;
import com.fiskmods.quantify.parser.SyntaxContext;
import com.fiskmods.quantify.parser.SyntaxParser;

class NamespaceParser implements SyntaxParser<JvmFunction> {
    static final SyntaxParser<JvmFunction> INSTANCE = new NamespaceParser();

    @Override
    public JvmFunction accept(QtfParser parser, SyntaxContext context) throws QtfParseException {
        parser.next(TokenClass.NAMESPACE);
        String namespaceName = parser.next(TokenClass.IDENTIFIER).getString();
        Namespace namespace;

        if (namespaceName.equals(Keywords.THIS)) {
            namespace = context.getDefaultNamespace();
        } else {
            namespace = Namespace.of(context.getMember(namespaceName, MemberType.LIBRARY))
                    .fallback(context.getDefaultNamespace());
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
