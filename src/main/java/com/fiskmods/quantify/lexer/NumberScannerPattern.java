package com.fiskmods.quantify.lexer;

public class NumberScannerPattern implements ScannerPattern {
    @Override
    public MatchResult match(String text, int startIndex) {
        char c = text.charAt(startIndex);
        if (!Character.isDigit(c)) {
            return null;
        }

        boolean decimal = false;
        int endIndex = startIndex + 1;
        for (; endIndex < text.length(); ++endIndex) {
            if ((c = text.charAt(endIndex)) == '.') {
                if (decimal) {
                    break;
                }
                decimal = true;
            } else if (!Character.isDigit(c)) {
                break;
            }
        }

        if (c == '.') {
            --endIndex;
        }
        return new MatchResult(text.substring(startIndex, endIndex));
    }
}
