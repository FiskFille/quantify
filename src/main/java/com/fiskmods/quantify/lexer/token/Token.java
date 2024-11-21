package com.fiskmods.quantify.lexer.token;

import com.fiskmods.quantify.exception.QtfParseException;

public record Token(TokenClass type, int startIndex, int endIndex, Object value) {
    public String getString() throws QtfParseException {
        if (value instanceof String) {
            return (String) value;
        }
        throw QtfParseException.internal("token '%s' is not a string".formatted(this), this);
    }

    public Operator getOperator() throws QtfParseException {
        if (value instanceof Operator) {
            return (Operator) value;
        }
        throw QtfParseException.internal("token '%s' is not an operator".formatted(this), this);
    }

    public Number getNumber() throws QtfParseException {
        if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (IllegalArgumentException e) {
                throw QtfParseException.internal("token '%s' can't be converted to a number"
                        .formatted(this), this);
            }
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        throw QtfParseException.internal("token '%s' is not a number".formatted(this), this);
    }

    @Override
    public String toString() {
        return value == null ? type.toString() : type + " " + value;
    }
}
