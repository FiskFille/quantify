package com.fiskmods.quantify.script;

import com.fiskmods.quantify.QtfSyntax;
import com.fiskmods.quantify.assembly.Assembler;
import com.fiskmods.quantify.exception.QtfAssemblyException;
import com.fiskmods.quantify.insn.InsnNode;
import com.fiskmods.quantify.insn.MemberInsnNode;
import com.fiskmods.quantify.interpreter.InterpreterStack;
import com.fiskmods.quantify.jvm.*;
import com.fiskmods.quantify.member.MemberMap;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import static com.fiskmods.quantify.insn.Instruction.NL;

public class QtfEvaluator {
    public final QtfSyntax syntax;

    private final List<InsnNode> nodes;
    private final MemberMap members;

    private int nodeOffset;

    public QtfEvaluator(QtfSyntax syntax, List<InsnNode> nodes, MemberMap members) {
        this.syntax = syntax;
        this.nodes = nodes;
        this.members = members;
    }

    public QtfEvaluator(QtfSyntax syntax, InterpreterStack.InterpretedScript script) {
        this(syntax, script.nodes(), script.members());
    }

    public FunctionAddress getFunction(MemberInsnNode node) throws QtfAssemblyException {
        if (node.id < 0 || node.id >= members.getFunctions().length) {
            throw error("function id out of bounds", node);
        }
        return members.getFunctions()[node.id];
    }

    public QtfScript compile(String name, DynamicClassLoader classLoader) throws QtfAssemblyException {
        try {
            JvmRunnable runnable = JvmCompiler.compile(assemble(), name, classLoader);
            return new QtfScript(runnable, members.createMemory());
        }
        catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new QtfAssemblyException(e.toString());
        }
    }

    // TODO: Re-add support for listeners
//    public QtfScript compile(QtfListener listener) throws QtfAssemblyException {
//        return new QtfScript(assemble(), members.createMemory(inputs, listener));
//    }

    private class AssemblyHelper {
        private List<InsnNode> list = nodes;
        public JvmFunction next() throws QtfAssemblyException {
            for (int i = 0; i < list.size(); ++i) {
                if (list.get(i).instruction != NL) {
                    continue;
                }
                List<InsnNode> line = list.subList(0, i);
                list = list.subList(i + 1, list.size());

                JvmFunction function = assembleLine(line, this::next);
                nodeOffset += line.size() + 1;
                return function;
            }
            return null;
        }
    }

    private JvmFunction assemble() throws QtfAssemblyException {
        AssemblyHelper helper = new AssemblyHelper();
        JvmFunction function = JvmFunction.EMPTY;
        JvmFunction next;

        while ((next = helper.next()) != null) {
            function = function.andThen(next);
        }
        return function;
    }

    private JvmFunction assembleLine(List<InsnNode> line, JvmFunction.Supplier nextLine) throws QtfAssemblyException {
        Assembler assembler = syntax.selectAssembler(line);
        if (assembler == null) {
            throw QtfAssemblyException.error("no assembly possible", nodeOffset, nodes);
        }
        return assembler.assemble(line, nextLine, this);
    }

    public QtfAssemblyException error(String desc, InsnNode node, int offset) {
        return QtfAssemblyException.error(desc, node.index + offset, nodes);
    }

    public QtfAssemblyException error(String desc, InsnNode node) {
        return error(desc, node, 0);
    }
}
