package com.fiskmods.quantify.lexer;

import com.fiskmods.quantify.exception.QtfLexerException;
import com.fiskmods.quantify.lexer.token.Operator;
import com.fiskmods.quantify.lexer.token.Token;

import java.util.function.Consumer;

import static com.fiskmods.quantify.lexer.token.Operator.*;
import static com.fiskmods.quantify.lexer.token.TokenClass.*;

public class QtfLexer {
    public static void read(String text, Consumer<Token> tokens) throws QtfLexerException {
        TextScanner scanner = new TextScanner(text);
        String result;

        try {
            while (scanner.hasNext()) {
                char c = scanner.peekChar();
                if (Character.isWhitespace(c)) {
                    scanner.advance();
                    if (isTerminator(c)) {
                        tokens.accept(scanner.newToken(TERMINATOR));
                    }
                    continue;
                }

                // Skip past comments
                if (scanner.next(ScannerPattern.COMMENT) != null) {
                    continue;
                }

                singleChar: {
                    scanner.advance();
                    switch (c) {
                        case '+' -> readOperator(scanner, tokens, ADD);
                        case '-' -> {
                            switch (scanner.peekChar()) {
                                case '=' -> {
                                    scanner.expand(1);
                                    tokens.accept(scanner.newToken(ASSIGNMENT, SUB));
                                }
                                case '>' -> {
                                    scanner.expand(1);
                                    tokens.accept(scanner.newToken(ASSIGNMENT, LERP));
                                }
                                case '\'' -> {
                                    scanner.expand(1);
                                    if (scanner.peekChar() == '>') {
                                        scanner.expand(1);
                                        tokens.accept(scanner.newToken(ASSIGNMENT, LERP_ROT));
                                    } else {
                                        scanner.skip(-2);
                                        scanner.advance();
                                        tokens.accept(scanner.newToken(OPERATOR, SUB));
                                    }
                                }
                                default -> tokens.accept(scanner.newToken(OPERATOR, SUB));
                            }
                        }
                        case '*' -> readOperator(scanner, tokens, MUL);
                        case '/' -> readOperator(scanner, tokens, DIV);
                        case '^' -> readOperator(scanner, tokens, POW);
                        case '%' -> readOperator(scanner, tokens, MOD);
                        case '&' -> {
                            if (readDoubleOperator(scanner, tokens, '&', AND)) {
                                break singleChar;
                            }
                        }
                        case '|' -> {
                            if (readDoubleOperator(scanner, tokens, '|', OR)) {
                                break singleChar;
                            }
                        }

                        case '<' -> readEqualityOperator(scanner, tokens, LT, LEQ);
                        case '>' -> readEqualityOperator(scanner, tokens, GT, GEQ);
                        case '!' -> {
                            if (scanner.peekChar() == '=') {
                                scanner.expand(1);
                                tokens.accept(scanner.newToken(OPERATOR, NEQ));
                            } else {
                                tokens.accept(scanner.newToken(NOT));
                            }
                        }
                        case '=' -> {
                            if (scanner.peekChar() == '=') {
                                scanner.expand(1);
                                tokens.accept(scanner.newToken(OPERATOR, EQ));
                            } else {
                                tokens.accept(scanner.newToken(ASSIGNMENT));
                            }
                        }

                        case '.' -> tokens.accept(scanner.newToken(DOT));
                        case ':' -> tokens.accept(scanner.newToken(COLON));
                        case ',' -> tokens.accept(scanner.newToken(COMMA));
                        case '(' -> tokens.accept(scanner.newToken(OPEN_PARENTHESIS));
                        case ')' -> tokens.accept(scanner.newToken(CLOSE_PARENTHESIS));
                        case '{' -> tokens.accept(scanner.newToken(OPEN_BRACES));
                        case '}' -> tokens.accept(scanner.newToken(CLOSE_BRACES));
                        case '[' -> tokens.accept(scanner.newToken(OPEN_BRACKETS));
                        case ']' -> tokens.accept(scanner.newToken(CLOSE_BRACKETS));
                        case '\'' -> tokens.accept(scanner.newToken(DEGREES));
                        default -> {
                            // Backtrack
                            scanner.skip(-1);
                            break singleChar;
                        }
                    }
                    continue;
                }

                if ((result = scanner.next(ScannerPattern.NUMBER)) != null) {
                    tokens.accept(scanner.newToken(NUM_LITERAL, result));
                    continue;
                }
                if ((result = scanner.next(ScannerPattern.STRING)) != null) {
                    tokens.accept(scanner.newToken(STR_LITERAL, result));
                    continue;
                }
                if ((result = scanner.next(ScannerPattern.IDENTIFIER)) != null) {
                    switch (result) {
                        // Keywords
                        case Keywords.DEF -> tokens.accept(scanner.newToken(DEF));
                        case Keywords.CONST -> tokens.accept(scanner.newToken(CONST));
                        case Keywords.IMPORT -> tokens.accept(scanner.newToken(IMPORT));
                        case Keywords.INPUT -> tokens.accept(scanner.newToken(INPUT));
                        case Keywords.OUTPUT -> tokens.accept(scanner.newToken(OUTPUT));
                        case Keywords.IF -> tokens.accept(scanner.newToken(IF));
                        case Keywords.ELSE -> tokens.accept(scanner.newToken(ELSE));
                        case Keywords.INTERPOLATE -> tokens.accept(scanner.newToken(INTERPOLATE));
                        case Keywords.NAMESPACE -> tokens.accept(scanner.newToken(NAMESPACE));

                        // Constants
                        case "pi" -> tokens.accept(scanner.newToken(NUM_LITERAL, Math.PI));
                        case "e" -> tokens.accept(scanner.newToken(NUM_LITERAL, Math.E));
                        case "NaN" -> tokens.accept(scanner.newToken(NUM_LITERAL, Double.NaN));
                        case "Inf" -> tokens.accept(scanner.newToken(NUM_LITERAL, Double.POSITIVE_INFINITY));
                        case "true" -> tokens.accept(scanner.newToken(NUM_LITERAL, 1));
                        case "false" -> tokens.accept(scanner.newToken(NUM_LITERAL, 0));

                        default -> tokens.accept(scanner.newToken(IDENTIFIER, result));
                    }
                    continue;
                }

                throw new QtfLexerException("Unknown symbol '%s'".formatted(c));
            }
        } catch (QtfLexerException e) {
            throw new QtfLexerException(e.getMessage() + " at " + TextScanner.fullTrace(scanner), e);
        }
    }

    private static void readOperator(TextScanner scanner, Consumer<Token> tokens, Operator operator) {
        if (scanner.peekChar() == '=') {
            scanner.expand(1);
            tokens.accept(scanner.newToken(ASSIGNMENT, operator));
            return;
        }
        tokens.accept(scanner.newToken(OPERATOR, operator));
    }

    private static boolean readDoubleOperator(TextScanner scanner, Consumer<Token> tokens, char c, Operator operator) {
        if (scanner.peekChar() == c) {
            scanner.expand(1);
            readOperator(scanner, tokens, operator);
            return false;
        }
        // Backtrack
        scanner.skip(-1);
        return true;
    }

    private static void readEqualityOperator(TextScanner scanner, Consumer<Token> tokens,
                                             Operator operator1, Operator operator2) {
        if (scanner.peekChar() == '=') {
            scanner.expand(1);
            tokens.accept(scanner.newToken(OPERATOR, operator2));
            return;
        }
        tokens.accept(scanner.newToken(OPERATOR, operator1));
    }

    public static boolean isNonAlphanumeric(char c) {
        return c != '_' && (c < 'a' || c > 'z') && (c < 'A' || c > 'Z') && (c < '0' || c > '9');
    }

    public static boolean isTerminator(char c) {
        return c == '\n' || c == '\r';
    }
}
