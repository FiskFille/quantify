package com.fiskmods.quantify.member;

import com.fiskmods.quantify.exception.QtfException;
import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.jvm.VariableType;
import com.fiskmods.quantify.parser.SyntaxContext;

public class FunctionScope extends Scope {
    private final String[] parameters;

    protected boolean hasReturnValue;

    private FunctionScope(Namespace namespace, String[] parameters, int level) {
        super(namespace, level);
        this.parameters = parameters;
    }

    public static FunctionScope create(SyntaxContext context, String[] parameters) throws QtfParseException {
        try {
            Scope prevScope = context.scope();
            FunctionScope scope = new FunctionScope(prevScope.namespace, parameters, 0);
            scope.lerpProgress = prevScope.lerpProgress;
            scope.localIndexOffset = 0;
            scope.members.inheritAllExcept(prevScope.members, MemberType.VARIABLE);

            for (String param : parameters) {
                scope.addLocalVariable(param, VariableType.PARAM);
            }
            return scope;
        } catch (QtfException e) {
            throw new QtfParseException(e);
        }
    }

    @Override
    public FunctionScope copy(Namespace namespace) {
        FunctionScope scope = new FunctionScope(namespace, parameters, level + 1);
        scope.lerpProgress = lerpProgress;
        scope.localIndexOffset = localIndexOffset;
        scope.members.inherit(members);
        return scope;
    }

    public void setHasReturnValue(boolean hasReturnValue) {
        this.hasReturnValue = hasReturnValue;
    }

    public boolean hasReturnValue() {
        return hasReturnValue;
    }

    @Override
    public boolean isParameter(String name) {
        for (String param : parameters) {
            if (name.equals(param)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isInnerScope() {
        return true;
    }
}
