package com.fiskmods.quantify.member;

import com.fiskmods.quantify.jvm.FunctionAddress;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class MemberMap {
    private final List<String> variables;
    private final FunctionAddress[] functions;
    private final Map<String, Integer> inputs;

    public MemberMap(Scope globalScope, FunctionAddress[] functions, Map<String, Integer> inputs) {
        variables = globalScope.getIdMap(MemberType.OUTPUT_VARIABLE);
        this.inputs = inputs;

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

    public Map<String, Integer> getInputs() {
        return inputs;
    }

    public QtfMemory createMemory(QtfListener listener) {
        QtfMemory memory = new QtfMemory(new double[variables.size()], functions);
        listener.listen(Variable.resolve(this, memory), variables::stream);
        return memory;
    }
}
