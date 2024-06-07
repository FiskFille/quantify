package com.fiskmods.quantify.script;

import com.fiskmods.quantify.QtfSyntax;
import com.fiskmods.quantify.assembly.Assembler;
import com.fiskmods.quantify.assembly.AssemblyRunnable;
import com.fiskmods.quantify.exception.QtfAssemblyException;
import com.fiskmods.quantify.insn.InsnNode;
import com.fiskmods.quantify.insn.MemberInsnNode;
import com.fiskmods.quantify.interpreter.InterpreterStack;
import com.fiskmods.quantify.member.QtfFunction;
import com.fiskmods.quantify.member.QtfListener;
import com.fiskmods.quantify.member.InputState;
import com.fiskmods.quantify.member.MemberMap;

import java.util.List;

import static com.fiskmods.quantify.insn.Instruction.NL;

public class QtfEvaluator {
    public final QtfSyntax syntax;

    private final List<InsnNode> nodes;
    private final MemberMap members;
    private final InputState inputs;

    private int nodeOffset;

    public QtfEvaluator(QtfSyntax syntax, List<InsnNode> nodes, MemberMap members, InputState inputs) {
        this.syntax = syntax;
        this.nodes = nodes;
        this.members = members;
        this.inputs = inputs;
    }

    public QtfEvaluator(QtfSyntax syntax, InterpreterStack.InterpretedScript script) {
        this(syntax, script.nodes(), script.members(), script.state().inputs());
    }

    public QtfFunction getFunction(MemberInsnNode node) throws QtfAssemblyException {
        if (node.id < 0 || node.id >= members.getFunctions().length) {
            throw error("function id out of bounds", node);
        }
        return members.getFunctions()[node.id];
    }

    public QtfScript compile() throws QtfAssemblyException {
        return new QtfScript(assemble(), members.createMemory(inputs));
    }

    public QtfScript compile(QtfListener listener) throws QtfAssemblyException {
        return new QtfScript(assemble(), members.createMemory(inputs, listener));
    }

    private class AssemblyHelper {
        private List<InsnNode> list = nodes;
        public AssemblyRunnable next() throws QtfAssemblyException {
            for (int i = 0; i < list.size(); ++i) {
                if (list.get(i).instruction != NL) {
                    continue;
                }
                List<InsnNode> line = list.subList(0, i);
                list = list.subList(i + 1, list.size());

                AssemblyRunnable runnable = assembleLine(line, this::next);
                nodeOffset += line.size() + 1;
                return runnable;
            }
            return null;
        }
    }

    private AssemblyRunnable assemble() throws QtfAssemblyException {
        AssemblyHelper helper = new AssemblyHelper();
        AssemblyRunnable runnable = AssemblyRunnable.EMPTY;
        AssemblyRunnable next;

        while ((next = helper.next()) != null) {
            runnable = runnable.andThen(next);
        }
        return runnable;
    }

    private AssemblyRunnable assembleLine(List<InsnNode> line, AssemblyRunnable.Supplier nextLine) throws QtfAssemblyException {
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
