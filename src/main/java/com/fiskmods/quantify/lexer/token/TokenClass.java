package com.fiskmods.quantify.lexer.token;

public enum TokenClass {
    TERMINATOR,

    // Constructs
    IDENTIFIER,
    OPERATOR,
    ASSIGNMENT,
    NUM_LITERAL,
    STR_LITERAL,

    // Syntax keywords
    VAR,
    CONST,
    FUNC,
    STRUCT,
    IMPORT,
    INPUT,
    PUBLIC,

    // Control keywords
    IF,
    ELSE,
    INTERPOLATE,
    NAMESPACE,
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
