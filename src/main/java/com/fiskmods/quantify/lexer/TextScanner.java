package com.fiskmods.quantify.lexer;

import com.fiskmods.quantify.exception.QtfLexerException;
import com.fiskmods.quantify.lexer.token.Token;
import com.fiskmods.quantify.lexer.token.TokenClass;

public class TextScanner {
    private final String text;

    private int scanIndex;
    private int scanLength;

    public TextScanner(String text) {
        this.text = text;
    }

    public int getScanIndex() {
        return scanIndex;
    }

    public int getStartIndex() {
        return scanIndex - scanLength;
    }

    public void advance() {
        ++scanIndex;
        scanLength = 1;
    }

    public void advance(ScannerPattern.MatchResult result) {
        scanLength = result.length();
        scanIndex += scanLength;
    }

    public void skip(int length) {
        scanLength = length;
        scanIndex += length;
    }

    /**
     * Expands the selection to include up to <code>length</code> more characters.
     * @param length the number of additional characters to include
     */
    public void expand(int length) {
        scanLength += length;
        scanIndex += length;
    }

    public Token newToken(TokenClass type, Object value) {
        return new Token(type, scanIndex - scanLength, scanIndex, value);
    }

    public Token newToken(TokenClass type) {
        return newToken(type, null);
    }

    public boolean hasNext() {
        return scanIndex < text.length();
    }

    public char nextChar() {
        return text.charAt(++scanIndex - 1);
    }

    public char peekChar() {
        return text.charAt(scanIndex);
    }

    public ScannerPattern.MatchResult peek(ScannerPattern pattern) throws QtfLexerException {
        return pattern.match(text, scanIndex);
    }

    public String next(ScannerPattern pattern) throws QtfLexerException {
        ScannerPattern.MatchResult result = peek(pattern);
        if (result == null) {
            return null;
        }
        advance(result);
        return result.match();
    }

    public static String address(String text, int scanIndex) {
        String s = text.substring(0, scanIndex);
        int line = 1, i;

        while ((i = s.indexOf('\n')) != -1) {
            s = s.substring(i + 1);
            ++line;
        }
        return "line %s, column %s".formatted(line, s.length() + 1);
    }

    public static String trace(String text, int scanIndex) {
        String s = text;
        int i, index = scanIndex;

        if ((i = s.indexOf('\n', index)) > -1) {
            s = s.substring(0, i);
        }
        if ((i = s.lastIndexOf('\n')) > -1) {
            s = s.substring(i + 1);
            index -= i + 1;
        }

        int start = Math.max(index - 64, 0);
        String s1 = s.substring(start, Math.min(index + 64, s.length()));
        return '\n' + " ".repeat(64 - index + start)
                + s1 + '\n' + " ".repeat(63) + " ^";
    }

    public static String fullTrace(String text, int scanIndex) {
        return address(text, scanIndex) + trace(text, scanIndex);
    }

    public static String fullTrace(TextScanner scanner) {
        return fullTrace(scanner.text, scanner.scanIndex);
    }
}
