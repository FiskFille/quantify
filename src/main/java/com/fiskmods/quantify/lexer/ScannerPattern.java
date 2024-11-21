package com.fiskmods.quantify.lexer;

import com.fiskmods.quantify.exception.QtfLexerException;

import java.util.function.Predicate;

@FunctionalInterface
public interface ScannerPattern {
    MatchResult match(String text, int startIndex) throws QtfLexerException;

    record MatchResult(String match, int length) {
        public MatchResult(String match) {
            this(match, match.length());
        }
    }

    ScannerPattern IDENTIFIER = (text, startIndex) -> {
        char c = text.charAt(startIndex);
        // Cannot begin with a digit
        if (Character.isDigit(c) || QtfLexer.isNonAlphanumeric(c)) {
            return null;
        }
        int l = startIndex;
        while (++l < text.length()) {
            if (QtfLexer.isNonAlphanumeric(text.charAt(l))) {
                break;
            }
        }
        return new MatchResult(text.substring(startIndex, l));
    };

    ScannerPattern TOKEN = (text, startIndex) -> {
        char c = text.charAt(startIndex);
        if (QtfLexer.isNonAlphanumeric(c)) {
            return new MatchResult(String.valueOf(c), 1);
        }
        int l = startIndex;
        while (++l < text.length()) {
            if (QtfLexer.isNonAlphanumeric(text.charAt(l))) {
                break;
            }
        }
        return new MatchResult(text.substring(startIndex, l));
    };

    ScannerPattern INTEGER = phrase(Character::isDigit);
    ScannerPattern NUMBER = new NumberScannerPattern();
    ScannerPattern STRING = new StringScannerPattern();
    ScannerPattern COMMENT = new CommentScannerPattern();

    static ScannerPattern phrase(Predicate<Character> allowedChar) {
        return (text, startIndex) -> {
            int l = startIndex;
            while (l < text.length()) {
                if (!allowedChar.test(text.charAt(l))) {
                    break;
                }
                ++l;
            }
            if (startIndex == l) {
                return null;
            }
            return new MatchResult(text.substring(startIndex, l));
        };
    }
}
