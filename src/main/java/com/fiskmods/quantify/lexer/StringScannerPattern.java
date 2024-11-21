package com.fiskmods.quantify.lexer;

import com.fiskmods.quantify.exception.QtfLexerException;

public class StringScannerPattern implements ScannerPattern {
    @Override
    public MatchResult match(String text, int startIndex) throws QtfLexerException {
        if (text.charAt(startIndex) != '"') {
            return null;
        }
        char c = '"', c1;
        int endIndex;
        loop: {
            for (endIndex = startIndex + 1; endIndex < text.length(); ++endIndex) {
                c1 = c;
                if ((c = text.charAt(endIndex)) == '"' && c1 != '\\') {
                    break loop;
                }
            }
            throw new QtfLexerException("Unclosed string");
        }
        String s = text.substring(startIndex + 1, endIndex);
        return new MatchResult(s.replace("\\\"", "\""), s.length() + 2);
    }
}
