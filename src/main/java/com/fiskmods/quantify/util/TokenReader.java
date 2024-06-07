package com.fiskmods.quantify.util;

import java.util.function.Predicate;

public class TokenReader {
    private final String text;

    private int scanIndex;
    private int markIndex = -1;

    public TokenReader(String text) {
        this.text = text;
    }

    public int getScanIndex() {
        return scanIndex;
    }

    public void setScanIndex(int scanIndex) {
        this.scanIndex = scanIndex;
    }

    public int getMarkIndex() {
        return markIndex;
    }

    public void setMarkIndex(int markIndex) {
        this.markIndex = markIndex;
    }

    public void mark() {
        markIndex = scanIndex;
    }

    public boolean hasMark() {
        return markIndex != -1;
    }

    public void reset() {
        scanIndex = markIndex;
        markIndex = -1;
    }

    public String getMarked() {
        return text.substring(markIndex, Math.min(scanIndex, text.length()));
    }

    public void skip(int length) {
        scanIndex += length;
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

    public String nextToken() {
        String s = peekToken();
        scanIndex += s.length();
        return s;
    }

    public String peekToken() {
        char c = text.charAt(scanIndex);
        if (QtfUtil.isNonAlphanumeric(c)) {
            return String.valueOf(c);
        }
        int l = scanIndex;
        while (++l < text.length()) {
            if (QtfUtil.isNonAlphanumeric(text.charAt(l))) {
                break;
            }
        }
        return text.substring(scanIndex, l);
    }

    public String nextIdentifier() {
        String s = peekIdentifier();
        scanIndex += s != null ? s.length() : 0;
        return s;
    }

    public String peekIdentifier() {
        char c = text.charAt(scanIndex);
        // Cannot begin with a digit
        if (Character.isDigit(c) || QtfUtil.isNonAlphanumeric(c)) {
            return null;
        }
        int l = scanIndex;
        while (++l < text.length()) {
            if (QtfUtil.isNonAlphanumeric(text.charAt(l))) {
                break;
            }
        }
        return text.substring(scanIndex, l);
    }

    public String nextPhrase(Predicate<Character> allowedChar) {
        String s = peekPhrase(allowedChar);
        scanIndex += s != null ? s.length() : 0;
        return s;
    }

    public String peekPhrase(Predicate<Character> allowedChar) {
        int l = scanIndex;
        while (l < text.length()) {
            if (!allowedChar.test(text.charAt(l))) {
                break;
            }
            ++l;
        }
        if (scanIndex == l) {
            return null;
        }
        return text.substring(scanIndex, l);
    }

    public String nextString() {
        String s = peekString();
        scanIndex += s != null ? s.length() + 2 : 0;
        return s;
    }

    public String peekString() {
        if (text.charAt(scanIndex) != '"') {
            return null;
        }
        char c = '"';
        int l = scanIndex + 1;
        l: {
            while (l < text.length()) {
                if (c != '\\' && (c = text.charAt(l)) == '"') {
                    break l;
                }
                ++l;
            }
            return null;
        }
        String s = text.substring(scanIndex + 1, l);
        return s.replace("\\\"", "\"");
    }

    public String peek(int length) {
        return length < 0 ? text.substring(Math.max(scanIndex + length, 0), scanIndex)
                : text.substring(scanIndex, Math.min(scanIndex + length, text.length()));
    }

    public void skipSpaces() {
        char c;
        while (hasNext() && ((c = peekChar()) == ' ' || c == '\t')) {
            ++scanIndex;
        }
    }

    public String address() {
        String s = text.substring(0, scanIndex);
        int line = 1, i;

        while ((i = s.indexOf('\n')) != -1) {
            s = s.substring(i + 1);
            ++line;
        }

        return String.format("line %s, column %s", line, s.length() + 1);
    }

    public String trace() {
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
        String s2 = "";

        for (i = 0, s2 += "\n"; i < 64 - index + start; ++i, s2 += " ");
        for (i = 0, s2 += s1 + "\n"; i < 64; ++i, s2 += " ");
        return s2 + "^";
    }

    public String fullTrace() {
        return address() + trace();
    }
}
