package com.fiskmods.quantify.parser.element;

import com.fiskmods.quantify.exception.QtfException;
import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.jvm.FunctionAddress;
import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.member.Namespace;
import com.fiskmods.quantify.parser.QtfParser;
import com.fiskmods.quantify.parser.SyntaxContext;
import com.fiskmods.quantify.parser.SyntaxParser;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.List;

record FunctionRef(FunctionAddress address, Value[] args, boolean hasResult) implements Value {
    public static SyntaxParser<FunctionRef> parser(FunctionAddress func, boolean hasResult) {
        return new FunctionRefParser(func, hasResult);
    }

    public static SyntaxParser<FunctionRef> parser(Namespace namespace, boolean hasResult) {
        return (parser, context) -> {
            try {
                String name = parser.next(TokenClass.IDENTIFIER).getString();
                return parser.next(parser(namespace.getFunction(name), hasResult));
            } catch (QtfException e) {
                throw new QtfParseException(e);
            }
        };
    }

    @Override
    public void apply(MethodVisitor mv) {
        address.run(mv, args);

        // Pop returned function value from stack if unused
        if (!hasResult) {
            mv.visitInsn(Opcodes.POP2);
        }
    }

    private record FunctionRefParser(FunctionAddress func, boolean hasResult) implements SyntaxParser<FunctionRef> {
        @Override
        public FunctionRef accept(QtfParser parser, SyntaxContext context) throws QtfParseException {
            parser.next(TokenClass.OPEN_PARENTHESIS);

            if (parser.isNext(TokenClass.CLOSE_PARENTHESIS)) {
                func.validateParameters(0, parser.next());
                return new FunctionRef(func, new Value[0], hasResult);
            }

            List<Value> args = parser.nextSequence(ExpressionParser.INSTANCE, TokenClass.COMMA);
            func.validateParameters(args.size(), parser.next(TokenClass.CLOSE_PARENTHESIS));

            if (!hasResult) {
                parser.next(TokenClass.TERMINATOR);
            }
            return new FunctionRef(func, args.toArray(new Value[0]), hasResult);
        }
    }
}
