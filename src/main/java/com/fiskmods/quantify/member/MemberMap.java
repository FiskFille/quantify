package com.fiskmods.quantify.member;

import com.fiskmods.quantify.util.QtfUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MemberMap {
    private final List<String> variables = new ArrayList<>();
    private final QtfFunction[] functions;

    public final int maxStackSize;

    public MemberMap(Scope globalScope, int maxS, QtfFunction[] functions) {
        List<String> out = globalScope.getIdMap(MemberType.OUTPUT_VARIABLE);
        variables.addAll(globalScope.getIdMap(MemberType.VARIABLE));
        maxStackSize = maxS + out.size();

        // Padding to make up the difference between global stack & max stack
        for (int i = variables.size(); i < maxS; ++i) {
            variables.add(null);
        }
        variables.addAll(out);

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

    public QtfFunction[] getFunctions() {
        return functions;
    }

    public QtfMemory createMemory(InputState inputs) {
        return new QtfMemory(new double[maxStackSize], functions, inputs.toArray());
    }

    public QtfMemory createMemory(InputState inputs, QtfListener listener) {
        QtfMemory memory = createMemory(inputs);
        listener.listen(Variable.resolve(this, memory), () -> variables.stream()
                .filter(t -> t != null && t.charAt(0) == QtfUtil.OUTPUT_PREFIX));
        return memory;
    }
}
