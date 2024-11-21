package com.fiskmods.quantify.parser.element;

import com.fiskmods.quantify.jvm.FunctionAddress;
import com.fiskmods.quantify.lexer.token.Token;
import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.library.QtfLibrary;
import com.fiskmods.quantify.member.QtfMemory;
import com.fiskmods.quantify.parser.QtfParser;
import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.parser.SyntaxContext;
import com.fiskmods.quantify.parser.SyntaxParser;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.List;

record FunctionRef(FunctionAddress address, Value[] params, boolean hasResult) implements Value {
    public static SyntaxParser<FunctionRef> parser(String name, QtfLibrary library, boolean hasResult) {
        return new FunctionRefParser(name, library, hasResult);
    }

    public static SyntaxParser<FunctionRef> parser(QtfLibrary library, boolean hasResult) {
        return (parser, context) -> {
            String name = parser.next(TokenClass.IDENTIFIER).getString();
            return parser.next(parser(name, library, hasResult));
        };
    }

    @Override
    public void apply(MethodVisitor mv) {
        QtfMemory.run(address, params).apply(mv);

        // Pop returned function value from stack if unused
        if (!hasResult) {
            mv.visitInsn(Opcodes.POP2);
        }
    }

    private record FunctionRefParser(String name, QtfLibrary library, boolean hasResult) implements SyntaxParser<FunctionRef> {
        @Override
        public FunctionRef accept(QtfParser parser, SyntaxContext context) throws QtfParseException {
            FunctionAddress func = library.getFunction(name);
            parser.next(TokenClass.OPEN_PARENTHESIS);

            if (parser.isNext(TokenClass.CLOSE_PARENTHESIS)) {
                Token token = parser.next();
                if (func.parameters > 0) {
                    throw new QtfParseException("Incorrect number of arguments for %s::%s"
                            .formatted(library.getKey(), name),
                            "expected %d, was 0".formatted(func.parameters), token);
                }
                return new FunctionRef(func, new Value[0], hasResult);
            }

            List<Value> params = new ArrayList<>();
            do {
                if (!params.isEmpty()) {
                    parser.next(TokenClass.COMMA);
                }
                params.add(parser.next(Expression::acceptEnclosed));
            } while (parser.isNext(TokenClass.COMMA));

            Token token = parser.next(TokenClass.CLOSE_PARENTHESIS);
            if (params.size() != func.parameters) {
                throw new QtfParseException("Incorrect number of arguments for %s::%s"
                        .formatted(library.getKey(), name),
                        "expected %d, was %d".formatted(func.parameters, params.size()), token);
            }
            if (!hasResult) {
                parser.next(TokenClass.TERMINATOR);
            }
            return new FunctionRef(func, params.toArray(new Value[0]), hasResult);
        }
    }
}
