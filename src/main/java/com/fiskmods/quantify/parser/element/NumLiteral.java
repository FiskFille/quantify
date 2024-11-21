package com.fiskmods.quantify.parser.element;

import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.parser.QtfParser;
import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.parser.SyntaxContext;
import com.fiskmods.quantify.parser.SyntaxParser;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.DCONST_0;
import static org.objectweb.asm.Opcodes.DCONST_1;

public record NumLiteral(double value) implements Value {
    public static final SyntaxParser<NumLiteral> PARSER = new NumLiteralParser();

    @Override
    public void apply(MethodVisitor mv) {
        if (value == 0) {
            mv.visitInsn(DCONST_0);
        } else if (value == 1) {
            mv.visitInsn(DCONST_1);
        } else {
            mv.visitLdcInsn(value);
        }
    }

    @Override
    public Value negate() {
        if (value == 0 || Double.isNaN(value)) {
            return this;
        }
        return new NumLiteral(-value);
    }

    private static class NumLiteralParser implements SyntaxParser<NumLiteral> {
        @Override
        public NumLiteral accept(QtfParser parser, SyntaxContext context) throws QtfParseException {
            double value = parser.next(TokenClass.NUM_LITERAL).getNumber().doubleValue();
            if (parser.isNext(TokenClass.DEGREES)) {
                parser.clearPeekedToken();
                value *= Math.PI / 180;
            }
            return new NumLiteral(value);
        }
    }
}
