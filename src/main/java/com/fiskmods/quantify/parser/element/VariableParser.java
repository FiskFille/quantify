package com.fiskmods.quantify.parser.element;

import com.fiskmods.quantify.exception.QtfException;
import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.jvm.VarAddress;
import com.fiskmods.quantify.jvm.assignable.VarType;
import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.member.Namespace;
import com.fiskmods.quantify.parser.SyntaxParser;

class VariableParser {
    static <T extends Value & Assignable> SyntaxParser<VarAddress<T>> refOrDef(
            VarType<T> type, boolean isDefinition) {
        return isDefinition ? def(type) : ref(type);
    }

    static <T extends Value & Assignable> SyntaxParser<VarAddress<T>> ref(VarType<T> type) {
        return IdentifierParser.from((name, namespace) ->
                (parser, context) -> compute(name, namespace, type, false));
    }

    static <T extends Value & Assignable> SyntaxParser<VarAddress<T>> def(VarType<T> type) {
        return (parser, context) -> {
            String name = parser.next(TokenClass.IDENTIFIER).getString();
            return parser.next(def(name, type));
        };
    }

    static <T extends Value & Assignable> SyntaxParser<VarAddress<T>> def(String name, VarType<T> type) {
        // Definitions always belong to the default namespace
        return (parser, context) ->
                compute(name, context.getDefaultNamespace(), type, true);
    }

    static <T extends Value & Assignable> VarAddress<T> compute(
            String name, Namespace namespace, VarType<T> type, boolean isDefinition)
            throws QtfParseException {
        try {
            return namespace.computeVariable(type, name, isDefinition);
        } catch (QtfException e) {
            throw new QtfParseException(e);
        }
    }
}
