package com.fiskmods.quantify.parser.element;

import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.parser.QtfParser;
import com.fiskmods.quantify.parser.SyntaxContext;
import com.fiskmods.quantify.parser.SyntaxParser;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

record Return(Value value) implements Value {
    static final ReturnParser INSTANCE = new ReturnParser();

    @Override
    public void apply(MethodVisitor mv) {
        value.apply(mv);
        mv.visitInsn(Opcodes.DRETURN);
    }

    record ReturnParser() implements SyntaxParser<Return> {
        @Override
        public Return accept(QtfParser parser, SyntaxContext context) throws QtfParseException {
            parser.clearPeekedToken();
            Value value = parser.next(ExpressionParser.INSTANCE);
            parser.skip(TokenClass.TERMINATOR);

            // Intentionally trigger exception if there are more tokens after return value
            if (!parser.isNext(TokenClass.CLOSE_BRACES)) {
                parser.next(TokenClass.CLOSE_BRACES);
            }
            return new Return(value);
        }
    }
}
