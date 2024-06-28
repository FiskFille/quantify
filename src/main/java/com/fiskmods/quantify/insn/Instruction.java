package com.fiskmods.quantify.insn;

import com.fiskmods.quantify.util.ScanDirection;

public interface Instruction {
    byte MASK_PROPS  = (byte) 0xE0;
    byte MASK_VALUE  = 0x1F;
    byte PROP_CONST  = 0x20;
    byte PROP_VAR    = 0x40;
    byte PROP_FUNC   = 0x60;
    byte PROP_OP     = (byte) 0x80;
    byte PROP_ASSIGN = (byte) 0xA0;

    // Markers
    byte NL = 0;
    byte BST = 1; // (
    byte BND = 2; // )
    byte NXT = 3; // ,

    // Control keywords
    byte END = 10;
    byte IF = 11;
    byte WTH = 12;
    byte NSP = 13;

    // Variable
    byte DEF = PROP_VAR;
    byte REF = PROP_VAR | 1;
    byte OUT = PROP_VAR | 2;
    byte IN  = PROP_VAR | 3;

    // Function
    byte FDEF = PROP_FUNC;
    byte FREF = PROP_FUNC | 1;
    byte FRUN = PROP_FUNC | 2;

    // Values
    byte CST = PROP_CONST;
    byte C_0 = PROP_CONST | 1;
    byte C_1 = PROP_CONST | 2;
    byte C_2 = PROP_CONST | 3;
    byte C_3 = PROP_CONST | 4;
    byte C_4 = PROP_CONST | 5;
    byte C_5 = PROP_CONST | 6;
    byte PI  = PROP_CONST | 7;
    byte E   = PROP_CONST | 8;
    byte NAN = PROP_CONST | 9;
    byte INF = PROP_CONST | 10;

    // Operators
    byte EQS = PROP_OP;
    byte ADD = PROP_OP | 1;
    byte SUB = PROP_OP | 2;
    byte MUL = PROP_OP | 3;
    byte DIV = PROP_OP | 4;
    byte POW = PROP_OP | 5;
    byte MOD = PROP_OP | 6;

    byte NEQ = PROP_OP | 8;
    byte LT  = PROP_OP | 9;
    byte GT  = PROP_OP | 10;
    byte LEQ = PROP_OP | 11;
    byte GEQ = PROP_OP | 12;

    byte AND = PROP_OP | 13;
    byte OR  = PROP_OP | 14;

    // Assignments
    byte EQ = PROP_ASSIGN;
    byte ADDEQ = PROP_ASSIGN | ADD & MASK_VALUE;
    byte SUBEQ = PROP_ASSIGN | SUB & MASK_VALUE;
    byte MULEQ = PROP_ASSIGN | MUL & MASK_VALUE;
    byte DIVEQ = PROP_ASSIGN | DIV & MASK_VALUE;
    byte POWEQ = PROP_ASSIGN | POW & MASK_VALUE;
    byte MODEQ = PROP_ASSIGN | MOD & MASK_VALUE;

    byte ANDEQ = PROP_ASSIGN | AND & MASK_VALUE;
    byte OREQ  = PROP_ASSIGN | OR  & MASK_VALUE;

    byte LRP = PROP_ASSIGN | 15;
    byte RLRP = PROP_ASSIGN | 16;

    static boolean isConstant(int instruction) {
        return (instruction & MASK_PROPS) == PROP_CONST;
    }

    static boolean isVariable(int instruction) {
        return (instruction & MASK_PROPS) == PROP_VAR;
    }

    static boolean isReference(int instruction) {
        return instruction == REF || instruction == OUT;
    }

    static boolean isValue(int instruction) {
        return isConstant(instruction) || isVariable(instruction) || instruction == FRUN;
    }

    static boolean isOperator(int instruction) {
        return (instruction & MASK_PROPS) == PROP_OP;
    }

    static boolean isAssignment(int instruction) {
        return (instruction & MASK_PROPS) == PROP_ASSIGN;
    }

    static boolean isExecution(int instruction) {
        return instruction == FREF || instruction == FRUN;
    }

    static boolean isClause(int instruction) {
        return instruction == IF || instruction == WTH;
    }

    static boolean isValueFrom(int instruction, ScanDirection direction) {
        return isValue(instruction) || (direction == ScanDirection.LEFT
                ? instruction == BST || instruction == FREF
                : instruction == BND);
    }

    static double getConstantValue(int instruction) {
        return switch (instruction) {
            case PI  -> Math.PI;
            case E   -> Math.E;
            case NAN -> Double.NaN;
            case INF -> Double.POSITIVE_INFINITY;
            default -> instruction - C_0;
        };
    }

    static boolean isNumber(int instruction) {
        return instruction >= CST && instruction <= C_5;
    }

    static boolean isAssignable(int instruction) {
        return isVariable(instruction);
    }

    static int toAssignment(int operator) {
        return (operator & MASK_VALUE) | PROP_ASSIGN;
    }

    static int toOperator(int assignment) {
        return (assignment & MASK_VALUE) | PROP_OP;
    }

    static String toString(int instruction) {
        return switch (instruction) {
            case NL -> "NL";
            case BST -> "BST";
            case BND -> "BND";
            case NXT -> "NXT";

            case END -> "END";
            case IF -> "IF";
            case WTH -> "WTH";
            case NSP -> "NSP";

            case DEF -> "DEF";
            case REF -> "REF";
            case OUT -> "OUT";
            case IN -> "IN";

            case FDEF -> "FDEF";
            case FREF -> "FREF";
            case FRUN -> "FRUN";

            case CST -> "CST";
            case C_0 -> "C_0";
            case C_1 -> "C_1";
            case C_2 -> "C_2";
            case C_3 -> "C_3";
            case C_4 -> "C_4";
            case C_5 -> "C_5";
            case PI  -> "PI";
            case E   -> "E";
            case NAN -> "NAN";
            case INF -> "INF";

            case ADD -> "ADD";
            case SUB -> "SUB";
            case MUL -> "MUL";
            case DIV -> "DIV";
            case POW -> "POW";
            case MOD -> "MOD";

            case EQS -> "EQS";
            case NEQ -> "NEQ";
            case LT  -> "LT";
            case GT  -> "GT";
            case LEQ -> "LEQ";
            case GEQ -> "GEQ";

            case AND -> "AND";
            case OR  -> "OR";

            case EQ -> "EQ";
            case ADDEQ -> "ADDEQ";
            case SUBEQ -> "SUBEQ";
            case MULEQ -> "MULEQ";
            case DIVEQ -> "DIVEQ";
            case POWEQ -> "POWEQ";
            case MODEQ -> "MODEQ";

            case ANDEQ -> "ANDEQ";
            case OREQ  -> "OREQ";

            case LRP -> "LRP";
            case RLRP -> "RLRP";

            default -> String.valueOf(instruction);
        };
    }
}
