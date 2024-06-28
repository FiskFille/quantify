package com.fiskmods.quantify;

import com.fiskmods.quantify.exception.QtfAssemblyException;
import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.interpreter.Interpreter;
import com.fiskmods.quantify.interpreter.InterpreterStack;
import com.fiskmods.quantify.jvm.DynamicClassLoader;
import com.fiskmods.quantify.library.QtfLibrary;
import com.fiskmods.quantify.script.QtfEvaluator;
import com.fiskmods.quantify.script.QtfScript;
import com.fiskmods.quantify.util.TokenReader;

import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;
import java.util.UUID;

import static com.fiskmods.quantify.exception.QtfParseException.unknownToken;

public class QtfParser {
    public static final boolean DEBUG = Boolean.parseBoolean(System.getProperty("com.fiskmods.quantify.Debug", "false"));

    public final QtfSyntax syntax;
    private final Map<String, QtfLibrary> libraries = new HashMap<>();

    private final DynamicClassLoader classLoader = new DynamicClassLoader();
    private int nextId = -1;

    public QtfParser(QtfSyntax syntax) {
        this.syntax = syntax;
    }

    public QtfParser() {
        this(QtfSyntax.DEFAULT);
    }

    public QtfParser addLibrary(QtfLibrary library) {
        libraries.put(library.getKey(), library);
        return this;
    }

    public QtfLibrary getLibrary(String key) {
        return libraries.get(key);
    }

    public int libraries() {
        return libraries.size();
    }

    private QtfEvaluator evaluate(String text) throws QtfParseException {
        InterpreterStack.InterpretedScript is = interpret(text);
        if (DEBUG) {
            System.out.println(QtfAssemblyException.createTrace(is.nodes(), -1));
            System.out.println(is.state().createTrace());
        }
        return new QtfEvaluator(syntax, is);
    }

    private String nextName() {
        return "Compiled" + ++nextId;
    }

    public QtfScript compile(String text) throws QtfParseException, QtfAssemblyException {
        return evaluate(text).compile(nextName(), classLoader);
    }

    // TODO: Re-add support for listeners
//    public QtfScript compile(String text, QtfListener listener) throws QtfParseException, QtfAssemblyException {
//        return evaluate(text).compile(listener);
//    }

    public InterpreterStack.InterpretedScript interpret(String text) throws QtfParseException {
        text = trimLines(text)
                .replace('\r', '\n')
                .replace('\t', ' ');

        enum Comment {
            NONE, LINE, BLOCK
        }

        TokenReader reader = new TokenReader(text);
        InterpreterStack stack = new InterpreterStack(this, reader);
        Comment comment = Comment.NONE;

        loop: while (reader.hasNext()) {
            char c = reader.peekChar();
            if (c == ' ') {
                reader.skip(1);
                continue;
            }

            if (comment == Comment.NONE) {
                if (c == '#') {
                    comment = Comment.LINE;
                    reader.skip(1);
                    continue;
                }
                else if (c == '/' && reader.hasNext() && reader.peek(2).equals("/#")) {
                    comment = Comment.BLOCK;
                    reader.skip(2);
                    continue;
                }
                for (Interpreter interpreter : stack) {
                    reader.mark();
                    if (interpreter.interpret(reader, stack)) {
                        continue loop;
                    }
                    reader.reset();
                }
            }
            else if (comment == Comment.BLOCK && c == '#' && reader.hasNext() && reader.peek(2).equals("#/")) {
                comment = Comment.NONE;
                reader.skip(2);
                stack.newLine();
                continue;
            }

            // Default newline, if no other logic is used
            if (c == '\n') {
                if (comment == Comment.LINE) {
                    comment = Comment.NONE;
                }
                stack.newLine();
                reader.skip(1);
                continue;
            }

            if (comment == Comment.NONE) {
                reader.mark();
                throw unknownToken(reader);
            }
            else {
                reader.skip(1);
            }
        }
        return stack.build();
    }

    private String trimLines(String text) {
        StringJoiner s = new StringJoiner("\n");
        for (String line : text.split("\n")) {
            s.add(line.stripTrailing());
        }
        return s.toString();
    }
}
