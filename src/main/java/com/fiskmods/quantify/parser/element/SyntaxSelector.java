package com.fiskmods.quantify.parser.element;

import com.fiskmods.quantify.exception.QtfException;
import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.jvm.FunctionAddress;
import com.fiskmods.quantify.jvm.VarAddress;
import com.fiskmods.quantify.lexer.token.Token;
import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.library.QtfLibrary;
import com.fiskmods.quantify.member.MemberMap;
import com.fiskmods.quantify.member.MemberType;
import com.fiskmods.quantify.member.Namespace;
import com.fiskmods.quantify.member.Struct;
import com.fiskmods.quantify.parser.QtfParser;
import com.fiskmods.quantify.parser.SyntaxContext;
import com.fiskmods.quantify.parser.SyntaxParser;

import java.util.Optional;

public class SyntaxSelector {
    public static SyntaxParser<?> selectSyntax(QtfParser parser, SyntaxContext context, Token next)
            throws QtfParseException {
        return switch (next.type()) {
            case IMPORT -> checkScope(ImportParser.INSTANCE, context, next);
            case INPUT -> checkScope(InputParser.INSTANCE, context, next);
            case OUTPUT -> checkScope(OutputParser.INSTANCE, context, next);
            case IF -> IfStatement.PARSER;
            case INTERPOLATE -> InterpolateStatement.PARSER;
            case NAMESPACE -> NamespaceParser.INSTANCE;
            case DEF -> DefParser.INSTANCE;
            case CONST -> ConstDefParser.INSTANCE;
            case STRUCT -> StructDefParser.INSTANCE;
            case RETURN -> Return.INSTANCE;

            case IDENTIFIER -> selectIdentifierSyntax(parser, context, next);
            default -> Assignment.PARSER;
        };
    }

    private static SyntaxParser<?> checkScope(SyntaxParser<?> syntaxParser, SyntaxContext context, Token next)
            throws QtfParseException {
        if (context.scope().isInnerScope()) {
            throw new QtfParseException("Illegal token '" + next + "'",
                    "unavailable in inner scopes", next);
        }
        return syntaxParser;
    }

    private static SyntaxParser<?> selectIdentifierSyntax(QtfParser parser, SyntaxContext context, Token next)
            throws QtfParseException {
        String name = next.getString();
        Optional<MemberMap.Member<?>> member = context.findMember(name);

        if (member.isEmpty()) {
            if (context.namespace().hasFunction(name)) {
                return FunctionRef.parser(context.namespace(), false);
            }
            return Assignment.PARSER;
        }

        MemberType<?> type = member.get().type();
        Namespace namespace;

        if (type == MemberType.LIBRARY) {
            namespace = Namespace.of((QtfLibrary) member.get().value());
        } else if (type == MemberType.STRUCT) {
            namespace = (Struct) member.get().value();
        } else if (type == MemberType.FUNCTION) {
            parser.clearPeekedToken(); // Advances past the identifier token
            FunctionAddress func = (FunctionAddress) member.get().value();
            return FunctionRef.parser(func, false);
        } else {
            return Assignment.PARSER;
        }

        parser.clearPeekedToken();
        parser.next(TokenClass.DOT);
        name = parser.next(TokenClass.IDENTIFIER).getString();

        if (namespace.hasFunction(name)) {
            try {
                return FunctionRef.parser(namespace.getFunction(name), false);
            } catch (QtfException e) {
                throw new QtfParseException(e);
            }
        }

        VarAddress var;
        try {
            var = namespace.computeVariable(name, false);
        } catch (QtfException e) {
            throw new QtfParseException(e);
        }
        Assignable target = parser.next(Assignable.parseList(var, false));
        return new Assignment.AssignmentParser(target, false);
    }
}
