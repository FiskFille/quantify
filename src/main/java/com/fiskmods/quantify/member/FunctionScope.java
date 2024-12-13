package com.fiskmods.quantify.member;

import com.fiskmods.quantify.exception.QtfException;
import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.parser.SyntaxContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FunctionScope extends Scope {
    private final String[] parameters;

    private FunctionScope(Namespace namespace, String[] parameters) {
        super(namespace);
        this.parameters = parameters;
    }

    public static FunctionScope create(SyntaxContext context, String[] parameters) throws QtfParseException {
        try {
            Scope prevScope = context.scope();
            FunctionScope scope = new FunctionScope(prevScope.namespace, parameters);
            scope.lerpProgress = prevScope.lerpProgress;

            for (Map.Entry<MemberType, List<String>> e : prevScope.ids.entrySet()) {
                MemberType type = e.getKey();
                if (type == MemberType.VARIABLE) {
                    continue;
                }
                scope.ids.put(type, e.getValue());
                e.getValue().forEach(k -> scope.types.put(k, type));
            }

            for (String param : parameters) {
                scope.put(param, MemberType.VARIABLE);
            }
            return scope;
        } catch (QtfException e) {
            throw new QtfParseException(e);
        }
    }

    @Override
    public FunctionScope copy(Namespace namespace) {
        FunctionScope scope = new FunctionScope(namespace, parameters);
        scope.lerpProgress = lerpProgress;
        scope.types.putAll(types);
        ids.forEach((k, v) -> scope.ids.put(k, new ArrayList<>(v)));
        return scope;
    }

    public boolean isParameter(String name) {
        for (String param : parameters) {
            if (name.equals(param)) {
                return true;
            }
        }
        return false;
    }
}
