package com.fiskmods.quantify.parser.element;

import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.parser.QtfParser;

class StructRefParser {
    static String expandName(QtfParser parser, String name) throws QtfParseException {
        StringBuilder nameBuilder = new StringBuilder(name);
        while ((name = IdentifierParser.nextName(parser)) != null) {
            nameBuilder.append('.')
                    .append(name);
        }
        return nameBuilder.toString();
    }
}
