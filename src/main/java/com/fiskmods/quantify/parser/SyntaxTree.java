package com.fiskmods.quantify.parser;

import com.fiskmods.quantify.jvm.JvmFunction;

import java.util.ArrayList;
import java.util.List;

public record SyntaxTree(SyntaxContext context, List<JvmFunction> elements) {
    public SyntaxTree(SyntaxContext context) {
        this(context, new ArrayList<>());
    }

    public JvmFunction flatten() {
        return mv -> elements.forEach(t -> t.apply(mv));
    }
}
