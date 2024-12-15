package com.fiskmods.quantify.parser.element;

import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.jvm.FunctionAddress;
import com.fiskmods.quantify.jvm.JvmClassComposer;
import com.fiskmods.quantify.jvm.JvmFunction;
import com.fiskmods.quantify.jvm.JvmFunctionDefinition;
import com.fiskmods.quantify.lexer.token.Token;
import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.member.FunctionScope;
import com.fiskmods.quantify.member.MemberType;
import com.fiskmods.quantify.parser.QtfParser;
import com.fiskmods.quantify.parser.SyntaxContext;
import com.fiskmods.quantify.parser.SyntaxParser;
import org.objectweb.asm.MethodVisitor;

import java.util.HashSet;
import java.util.Set;

import static org.objectweb.asm.Opcodes.*;

record FunctionDef(DefinedFunctionAddress address, JvmFunction body, ReturnValueType returnValue)
        implements JvmFunctionDefinition {
    @Override
    public JvmClassComposer define(String className) {
        address.owner = className;
        return cw -> {
            MethodVisitor mv = cw.visitMethod(ACC_STATIC | ACC_PRIVATE, address.name, address.descriptor, null, null);
            body.apply(mv);

            if (returnValue == ReturnValueType.MISSING) {
                mv.visitInsn(DCONST_0);
                mv.visitInsn(DRETURN);
            } else if (returnValue == ReturnValueType.IMPLICIT) {
                mv.visitInsn(DRETURN);
            }
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        };
    }

    record FunctionDefParser(String name) implements SyntaxParser<JvmFunction> {
        @Override
        public JvmFunction accept(QtfParser parser, SyntaxContext context) throws QtfParseException {
            DefinedFunctionAddress address = new DefinedFunctionAddress();
            context.addMember(name, MemberType.FUNCTION, address);

            parser.next(TokenClass.OPEN_PARENTHESIS);
            String[] parameters = parseParameters(parser);
            FunctionScope scope = FunctionScope.create(context, parameters);
            JvmFunction body;
            ReturnValueType returnValue;

            address.descriptor = FunctionAddress.descriptor(parameters.length);
            address.parameters = parameters.length;

            if (parser.isNext(TokenClass.ASSIGNMENT)) {
                Token assignment = parser.next(TokenClass.ASSIGNMENT);
                if (assignment.value() != null) {
                    throw QtfParseException.error("function definitions can't use assignment operators", assignment);
                }

                context.push(scope);
                body = parser.next(ExpressionParser.INSTANCE);
                returnValue = ReturnValueType.IMPLICIT;
                context.pop();
            } else {
                body = parser.next(new FunctionBody.FunctionBodyParser(scope));
                returnValue = scope.hasReturnValue() ? ReturnValueType.EXPLICIT : ReturnValueType.MISSING;
            }

            int index = context.defineFunction(new FunctionDef(address, body, returnValue));
            address.name = "f" + index + "_" + name;
            return null;
        }

        private String[] parseParameters(QtfParser parser) throws QtfParseException {
            // Function has no parameters
            if (parser.isNext(TokenClass.CLOSE_PARENTHESIS)) {
                parser.clearPeekedToken();
                return new String[0];
            }

            Set<String> set = new HashSet<>();
            while (true) {
                Token token = parser.next(TokenClass.IDENTIFIER);
                if (!set.add(token.getString())) {
                    throw QtfParseException.error("duplicate parameter '%s'".formatted(token.getString()), token);
                }

                if (parser.isNext(TokenClass.COMMA)) {
                    parser.clearPeekedToken();
                    continue;
                }
                break;
            }

            parser.next(TokenClass.CLOSE_PARENTHESIS);
            return set.toArray(new String[0]);
        }
    }

    enum ReturnValueType {
        MISSING, IMPLICIT, EXPLICIT
    }

    private static class DefinedFunctionAddress implements FunctionAddress {
        private String owner;
        private String name;
        private String descriptor;
        private int parameters;

        @Override
        public String owner() {
            return owner;
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public String descriptor() {
            return descriptor;
        }

        @Override
        public int parameters() {
            return parameters;
        }
    }
}
