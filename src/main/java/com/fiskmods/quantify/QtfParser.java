package com.fiskmods.quantify;

import com.fiskmods.quantify.exception.QtfAssemblyException;
import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.interpreter.Interpreter;
import com.fiskmods.quantify.interpreter.InterpreterStack;
import com.fiskmods.quantify.jvm.DynamicClassLoader;
import com.fiskmods.quantify.library.QtfLibrary;
import com.fiskmods.quantify.member.QtfListener;
import com.fiskmods.quantify.script.QtfEvaluator;
import com.fiskmods.quantify.script.QtfScript;
import com.fiskmods.quantify.util.TokenReader;

import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;
import java.util.function.Supplier;

import static com.fiskmods.quantify.exception.QtfParseException.unknownToken;

public class QtfParser {
    public static final boolean DEBUG = Boolean.parseBoolean(System.getProperty("com.fiskmods.quantify.Debug", "false"));

    public final QtfSyntax syntax;
    private final Map<String, QtfLibrary> libraries = new HashMap<>();
    private final Supplier<DynamicClassLoader> classLoaderFactory;

    private DynamicClassLoader classLoader;
    private int nextClassId = -1;

    public QtfParser(QtfSyntax syntax, Supplier<DynamicClassLoader> classLoader) {
        this.syntax = syntax;
        this.classLoaderFactory = classLoader;
    }

    public QtfParser(QtfSyntax syntax) {
        this(syntax, DynamicClassLoader::new);
    }

    public QtfParser(Supplier<DynamicClassLoader> classLoader) {
        this(QtfSyntax.DEFAULT, classLoader);
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
        return "com.fiskmods.quantify.dynamic.Compiled" + ++nextClassId;
    }

    public QtfScript compile(String text, QtfListener listener) throws QtfParseException, QtfAssemblyException {
        if (classLoader == null) {
            classLoader = classLoaderFactory.get();
        }
        return evaluate(text).compile(nextName(), classLoader, listener);
    }

    public QtfScript compile(String text) throws QtfParseException, QtfAssemblyException {
        return compile(text, QtfListener.IGNORE);
    }

    /**
     * Triggers garbage collection on the ClassLoader so that any scripts
     * loaded from it can be released from memory.
     */
    public void flush() {
        classLoader = null;
        System.gc();
    }

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
