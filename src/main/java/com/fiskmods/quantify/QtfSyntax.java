package com.fiskmods.quantify;

import com.fiskmods.quantify.assembly.*;
import com.fiskmods.quantify.insn.InsnNode;
import com.fiskmods.quantify.interpreter.*;
import com.fiskmods.quantify.validator.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static com.fiskmods.quantify.interpreter.Interpreter.BRACKETS;
import static com.fiskmods.quantify.interpreter.Interpreter.COMMA;

public final class QtfSyntax {
    private static final List<Interpreter> EXPRESSION = Arrays.asList(
            NewLineInterpreter.INSTANCE,
            OpInterpreter.INSTANCE,
            BRACKETS,
            COMMA,
            ConstInterpreter.INSTANCE,
            new NameInterpreter(true, true, true),
            MergeInterpreter.INSTANCE
    );

    public static final QtfSyntax DEFAULT = builder()
            .rule(AssignmentLineValidator.FACTORY, Arrays.asList(
                    NewLineInterpreter.INSTANCE,
                    AssignmentInterpreter.LENIENT,
                    LerpInterpreter.INSTANCE,
                    COMMA,
                    OpInterpreter.INSTANCE,
                    new NameInterpreter(true, false, false),
                    MergeInterpreter.INSTANCE
            ))
            .rule(ExpressionLineValidator.FACTORY, EXPRESSION)
            .rule(ParameterLineValidator.FACTORY, EXPRESSION)
            .rule(ExecutionLineValidator.FACTORY, Arrays.asList(
                    NewLineInterpreter.INSTANCE,
                    BRACKETS,
                    MergeInterpreter.INSTANCE
            ))
            .rule(ClauseLineValidator.FACTORY, EXPRESSION)
            .rule(NamespaceLineValidator.FACTORY, List.of(KeywordInterpreter.INSTANCE))
            .rule(EndLineValidator.FACTORY, List.of())
            .assemblers(
                    ClauseAssembler.FACTORY,
                    ExecutionAssembler.FACTORY,
                    AssignmentAssembler.FACTORY
            )
            .build(Arrays.asList(
                    //DefFuncInterpreter.INSTANCE,
                    KeywordInterpreter.INSTANCE,
                    OpInterpreter.INSTANCE,
                    new NameInterpreter(true, true, true)
            ));

    private final List<SyntaxRule> rules;
    private final List<AssemblerFactory> assemblers;
    private final Iterable<Interpreter> interpreters;

    private QtfSyntax(List<SyntaxRule> rules, List<AssemblerFactory> assemblers, Iterable<Interpreter> interpreters) {
        this.rules = rules;
        this.assemblers = assemblers;
        this.interpreters = interpreters;
    }

    public Iterable<Interpreter> interpreters() {
        return interpreters;
    }

    public SyntaxRule selectRule(InterpreterStack stack, LinkedList<InsnNode> line, LineValidator prev, InsnNode next) {
        for (SyntaxRule rule : rules) {
            if (rule.factory.isApplicable(stack, line, prev, next)) {
                return rule;
            }
        }
        return null;
    }

    public Assembler selectAssembler(List<InsnNode> line) {
        Assembler assembler;
        for (AssemblerFactory factory : assemblers) {
            if ((assembler = factory.get(line)) != null) {
                return assembler;
            }
        }
        return null;
    }

    public static Builder builder() {
        return new Builder();
    }

    public record SyntaxRule(LineValidatorFactory factory, Iterable<Interpreter> interpreters) {
    }

    public static class Builder {
        private final List<SyntaxRule> rules = new ArrayList<>();
        private final List<AssemblerFactory> assemblers = new ArrayList<>();

        public Builder rule(LineValidatorFactory factory, List<Interpreter> interpreters) {
            rules.add(new SyntaxRule(factory, interpreters));
            return this;
        }

        public Builder assembler(AssemblerFactory factory) {
            assemblers.add(factory);
            return this;
        }

        public Builder assemblers(AssemblerFactory... factories) {
            assemblers.addAll(Arrays.asList(factories));
            return this;
        }

        public QtfSyntax build(Iterable<Interpreter> interpreters) {
            return new QtfSyntax(new ArrayList<>(rules), assemblers, interpreters);
        }
    }
}
