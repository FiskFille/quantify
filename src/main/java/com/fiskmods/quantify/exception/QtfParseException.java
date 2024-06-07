package com.fiskmods.quantify.exception;

import com.fiskmods.quantify.util.TokenReader;

public class QtfParseException extends QtfException {
    public QtfParseException() {
    }

    public QtfParseException(String message) {
        super(message);
    }

    public QtfParseException(Throwable cause) {
        super(cause);
    }

    private static String resetReader(TokenReader reader) {
        if (reader.hasMark() && reader.getMarkIndex() != reader.getScanIndex()) {
            String s = reader.getMarked();
            reader.reset();
            return s;
        }
        if (reader.hasNext()) {
            return reader.peekToken();
        }
        return null;
    }

    public static QtfParseException unexpectedTokenOrError(TokenReader reader, String reason) {
        if (!reader.hasNext()) {
            return error(reader, reason);
        }
        reader.skip(-1);
        if (reader.peekChar() == '\n') {
            reader.skip(1);
            return error(reader, reason);
        }
        reader.skip(1);
        return unexpectedToken(reader, reason);
    }

    public static QtfParseException error(TokenReader reader, String issue) {
        resetReader(reader);
        return new QtfParseException("Unresolved issue at %s: "
                .formatted(reader.address()) + issue + reader.trace());
    }

    public static QtfParseException invalidToken(TokenReader reader, String reason) {
        String scan = resetReader(reader);
        return new QtfParseException("Invalid token '%s' at %s: "
                .formatted(scan, reader.address()) + reason + reader.trace());
    }

    public static QtfParseException unexpectedToken(TokenReader reader, String reason) {
        String scan = resetReader(reader);
        return new QtfParseException("Unexpected token '%s' at %s: "
                .formatted(scan, reader.address()) + reason + reader.trace());
    }

    public static QtfParseException unknownToken(TokenReader reader) {
        String scan = resetReader(reader);
        return new QtfParseException("Unknown token '%s' at %s: "
                .formatted(scan, reader.address()) + reader.trace());
    }
}
