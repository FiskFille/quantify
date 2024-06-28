package com.fiskmods.quantify.member;

import com.fiskmods.quantify.jvm.FunctionAddress;

import java.util.Arrays;
import java.util.List;

public class MemberMap {
    private final List<String> variables;
    private final FunctionAddress[] functions;

    public MemberMap(Scope globalScope, FunctionAddress[] functions) {
        variables = globalScope.getIdMap(MemberType.OUTPUT_VARIABLE);

        int l = functions.length;
        while (l > 0 && functions[l - 1] == null) {
            --l;
        }

        if (l != functions.length) {
            this.functions = Arrays.copyOfRange(functions, 0, l);
        }
        else {
            this.functions = functions;
        }
    }

    public int getVariableId(String name) {
        return variables.indexOf(name);
    }

    public FunctionAddress[] getFunctions() {
        return functions;
    }

    public QtfMemory createMemory() {
        return new QtfMemory(new double[variables.size()], functions);
    }

    public QtfMemory createMemory(QtfListener listener) {
        QtfMemory memory = createMemory();
        listener.listen(Variable.resolve(this, memory), variables::stream);
        return memory;
    }
}
