package com.fiskmods.quantify.lexer;

public class CommentScannerPattern implements ScannerPattern {
    @Override
    public MatchResult match(String text, int startIndex) {
        return switch (text.charAt(startIndex)) {
            case '#' -> matchLine(text, startIndex);
            case '/' -> matchBlock(text, startIndex);
            default -> null;
        };
    }

    private MatchResult matchLine(String text, int startIndex) {
        int endIndex = startIndex + 1;
        while (endIndex < text.length() && !QtfLexer.isTerminator(text.charAt(endIndex))) {
            ++endIndex;
        }
        return new MatchResult(text.substring(startIndex, endIndex));
    }

    private MatchResult matchBlock(String text, int startIndex) {
        if (startIndex + 1 >= text.length() || text.charAt(startIndex + 1) != '#') {
            return null;
        }

        int endIndex = startIndex + 2;
        char c = '#', c1;
        for (; endIndex < text.length(); ++endIndex) {
            c1 = c;
            if ((c = text.charAt(endIndex)) == '/' && c1 == '#') {
                break;
            }
        }
        return new MatchResult(text.substring(startIndex, endIndex + 1));
    }
}
