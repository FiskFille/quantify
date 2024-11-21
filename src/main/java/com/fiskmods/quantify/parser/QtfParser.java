package com.fiskmods.quantify.parser;

import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.lexer.token.Token;
import com.fiskmods.quantify.lexer.token.TokenClass;
import com.fiskmods.quantify.parser.element.SyntaxSelector;

import java.util.Iterator;
import java.util.Objects;

public class QtfParser {
    private final Iterator<Token> tokenStream;
    private final SyntaxContext context;

    private Token peekedToken, lastToken;

    public QtfParser(Iterator<Token> tokenStream, SyntaxContext context) {
        this.tokenStream = tokenStream;
        this.context = context;
    }

    public void parse(SyntaxTree syntaxTree, boolean isEnclosed) throws QtfParseException {
        while (tokenStream.hasNext()) {
            if (isEnclosed && peek().type() == TokenClass.CLOSE_BRACES) {
                return;
            }
            if (peek().type() == TokenClass.TERMINATOR) {
                clearPeekedToken();
                continue;
            }

            SyntaxParser<?> syntax = SyntaxSelector.selectSyntax(this, context, peek());
            SyntaxElement element = next(syntax);
            if (element != null) {
                syntaxTree.elements().add(element);
            }
        }
    }

    /**
     * Clears the last peeked token, allowing any subsequent {@link QtfParser#peek()}
     * call to move on to a different token.
     */
    public void clearPeekedToken() {
        peekedToken = null;
    }

    /**
     * Gets the next token in the stream without advancing to the next.
     *
     * @return the next token that will be returned by {@link QtfParser#next()},
     *          or <code>null</code> if no more tokens remain
     */
    public Token peek() {
        if (peekedToken != null) {
            return peekedToken;
        }
        return peekedToken = (tokenStream.hasNext() ? tokenStream.next() : null);
    }

    /**
     * Gets the last token that was consumed from the stream.
     *
     * @return the last consumed token, or <code>null</code> if no token has been consumed yet
     */
    public Token last() {
        return lastToken;
    }

    /**
     * Checks whether any tokens remain in the token stream.
     *
     * @return <code>true</code> if there are more tokens to be consumed
     */
    public boolean hasNext() {
        return tokenStream.hasNext();
    }

    /**
     * Checks whether any tokens remain in the token stream, within the specified
     * boundary. If none remain, the peeked token state is cleared.
     *
     * @param boundary the boundary within which to check for more tokens
     * @return <code>true</code> if there are more tokens
     */
    public boolean hasNext(Boundary boundary) {
        Token peeked = peek();
        if (peeked == null || peeked.type() == TokenClass.TERMINATOR) {
            clearPeekedToken();
            return false;
        }
        return boundary != Boundary.CLOSURE
                || peeked.type() != TokenClass.CLOSE_PARENTHESIS
                && peeked.type() != TokenClass.CLOSE_BRACES
                && peeked.type() != TokenClass.COMMA;
    }

    /**
     * Gets the next token in the token stream.
     *
     * @return the next token in the stream
     * @throws java.util.NoSuchElementException if no more tokens remain
     */
    public Token next() {
        if (peekedToken != null) {
            lastToken = peekedToken;
            peekedToken = null;
            return lastToken;
        }
        return lastToken = tokenStream.next();
    }

    /**
     * Gets the next token in the stream, provided that it matches the expected
     * token class.
     *
     * @param expectedClass the token class to check for
     * @return the next token in the stream
     * @throws QtfParseException if no more tokens remain, or if the next token doesn't match
     */
    public Token next(TokenClass expectedClass) throws QtfParseException {
        if (!hasNext()) {
            throw new QtfParseException("End of token sequence", "expected " + expectedClass, null);
        }
        Token next = next();
        if (next.type() == expectedClass) {
            return next;
        }
        throw new QtfParseException("Unexpected token '" + next + "'", "expected " + expectedClass, next);
    }

    /**
     * Checks if the next token in the stream is of the specified token class.
     *
     * @param expectedClass the token class to check for
     * @return <code>true</code> if there are more tokens, and the next matches requirements
     */
    public boolean isNext(TokenClass expectedClass) {
        Token peeked = peek();
        return peeked != null && peeked.type() == expectedClass;
    }

    /**
     * Checks if the next token in the stream is of the specified token class,
     * with the specified value attached. <code>value</code> may be <code>null</code>.
     *
     * @param expectedClass the token class to check for
     * @param expectedValue the token value to check for, or <code>null</code>
     * @return <code>true</code> if there are more tokens, and the next matches requirements
     */
    public boolean isNext(TokenClass expectedClass, Object expectedValue) {
        Token peeked = peek();
        return peeked != null && peeked.type() == expectedClass
                && Objects.equals(expectedValue, peeked.value());
    }

    /**
     * Skips past as many tokens in a row as needed, so long as they match
     * the given token class.
     *
     * @param tokenClass the token class to check for
     * @return <code>true</code> if any tokens were skipped
     */
    public boolean skip(TokenClass tokenClass) {
        boolean flag = false;
        while (hasNext() && peek().type() == tokenClass) {
            clearPeekedToken();
            flag = true;
        }
        return flag;
    }

    public <T extends SyntaxElement> T next(SyntaxParser<T> syntaxParser) throws QtfParseException {
        return syntaxParser.accept(this, context);
    }

    public enum Boundary {
        LINE, CLOSURE
    }
}
