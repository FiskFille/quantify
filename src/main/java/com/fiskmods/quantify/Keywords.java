package com.fiskmods.quantify;

import static com.fiskmods.quantify.insn.Instruction.*;

public interface Keywords {
    String DEF = "def";
    String THIS = "this";
    String IMPORT = "import";
    String INPUT = "input";
    String OUTPUT = "output";
    String FUNC = "func";
    String END = "end";
    String IF = "if";
    String WITH = "with";
    String USING = "using";

    // Keywords not currently in use, but reserved for potential future use
    String VAR = "var";
    String CONST = "const";
    String FOR = "for";
    String WHILE = "while";
    String DO = "do";
    String THEN = "then";
    String RETURN = "return";
    String SWITCH = "switch";
    String CASE = "case";
    String DEFAULT = "default";
    String WHEN = "when";
    String TRY = "try";
    String CATCH = "catch";

    static boolean isKeyword(String s) {
        return switch (s) {
            case DEF, THIS, IMPORT, INPUT, OUTPUT, FUNC, END, IF, WITH, USING,
                    VAR, CONST, FOR, WHILE, DO, THEN, RETURN, SWITCH, CASE, DEFAULT, WHEN, TRY, CATCH -> true;
            default -> false;
        };
    }

    static boolean isConstVar(String s) {
        return switch (s) {
            case "pi", "e", "true", "false", "NaN", "Inf" -> true;
            default -> false;
        };
    }

    static int getConstVar(String s) {
        return switch(s) {
            case "pi" -> PI;
            case "e" -> E;
            case "NaN" -> NAN;
            case "Inf" -> INF;
            case "true" -> C_1;
            case "false" -> C_0;
            default -> -1;
        };
    }

    static boolean isNameReserved(String name) {
        return isKeyword(name) || isConstVar(name);
    }
}
