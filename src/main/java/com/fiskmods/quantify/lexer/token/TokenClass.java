package com.fiskmods.quantify.lexer.token;

public enum TokenClass {
    TERMINATOR,

    // Constructs
    IDENTIFIER,
    OPERATOR,
    ASSIGNMENT,
    NUM_LITERAL,
    STR_LITERAL,

    // Keywords
    IMPORT,
    INPUT,
    OUTPUT,
    IF,
    ELSE,
    INTERPOLATE,
    NAMESPACE,
    DEF,
    CONST,
    RETURN,

    // Symbols
    DOT,
    COLON,
    COMMA,
    NOT,
    DEGREES,
    OPEN_PARENTHESIS,
    CLOSE_PARENTHESIS,
    OPEN_BRACES,
    CLOSE_BRACES,
    OPEN_BRACKETS,
    CLOSE_BRACKETS
}
