package com.fiskmods.quantify.util;

import com.fiskmods.quantify.Keywords;
import com.fiskmods.quantify.exception.QtfParseException;

import java.util.function.Function;

public class QtfUtil {
    public static String extractToken(TokenReader reader, Function<TokenReader, String> func, String errorMessage)
            throws QtfParseException {
        reader.skipSpaces();
        reader.mark();
        String value;
        if (!reader.hasNext() || (value = func.apply(reader)) == null) {
            throw QtfParseException.unexpectedTokenOrError(reader, errorMessage);
        }
        return value;
    }

    public static String extractName(TokenReader reader) throws QtfParseException {
        return checkNameValid(reader,
                extractToken(reader, TokenReader::nextIdentifier, "expected name"));
    }

    public static String extractString(TokenReader reader) throws QtfParseException {
        return extractToken(reader, TokenReader::nextString, "expected string");
    }

    public static int extractInteger(TokenReader reader) throws QtfParseException {
        String s = extractToken(reader, t -> t.nextPhrase(Character::isDigit), "expected integer");
        return Integer.parseInt(s);
    }

    public static char peekChar(TokenReader reader) {
        int i = reader.getScanIndex();
        reader.skipSpaces();
        if (!reader.hasNext()) {
            return 0;
        }
        char c = reader.peekChar();
        reader.setScanIndex(i);
        return c;
    }

    public static void consumeChar(TokenReader reader, char c)
            throws QtfParseException {
        reader.skipSpaces();
        reader.mark();
        if (!reader.hasNext() || reader.nextChar() != c) {
            throw QtfParseException.unexpectedTokenOrError(reader, "expected '%s'".formatted(c));
        }
    }

    public static boolean isNonAlphanumeric(char c) {
        return c != '_' && (c < 'a' || c > 'z') && (c < 'A' || c > 'Z') && (c < '0' || c > '9');
    }

    public static String checkNameValid(TokenReader reader, String name) throws QtfParseException {
        if (Keywords.isNameReserved(name)) {
            throw QtfParseException.invalidToken(reader,
                    "'%s' is a reserved keyword, it cannot be used as a name".formatted(name));
        }
        return name;
    }
}
