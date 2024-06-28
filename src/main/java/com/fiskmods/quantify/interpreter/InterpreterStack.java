package com.fiskmods.quantify.interpreter;

import com.fiskmods.quantify.QtfParser;
import com.fiskmods.quantify.QtfSyntax;
import com.fiskmods.quantify.exception.QtfParseException;
import com.fiskmods.quantify.insn.InsnNode;
import com.fiskmods.quantify.insn.Instruction;
import com.fiskmods.quantify.member.MemberMap;
import com.fiskmods.quantify.util.TokenReader;
import com.fiskmods.quantify.validator.LineValidator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

import static com.fiskmods.quantify.exception.QtfParseException.unexpectedToken;

public class InterpreterStack implements Iterable<Interpreter> {
    private final List<InsnNode> nodes = new ArrayList<>();
    private final LinkedList<InsnNode> currLine = new LinkedList<>();

    private final InterpreterState state;

    public final QtfParser parser;
    public final TokenReader reader;

    private QtfSyntax.SyntaxRule rule;
    private LineValidator validator;

    public InterpreterStack(QtfParser parser, TokenReader reader) {
        this.parser = parser;
        this.reader = reader;
        state = new InterpreterState(this);
    }

    public InterpreterState state() {
        return state;
    }

    public void recalculateSyntax(InsnNode next) throws QtfParseException {
        if ((rule = parser.syntax.selectRule(this, currLine, validator, next)) == null) {
            if (currLine.isEmpty()) {
                throw unexpectedToken(reader, "expected variable");
            }
            throw unexpectedToken(reader, "unknown syntax");
        }
        validator = rule.factory().create(validator);
    }

    public void newLine() throws QtfParseException {
        if (currLine.isEmpty()) {
            return;
        }
        if (validator != null) {
            validator.endLine(this, currLine.getLast());
        }
        nodes.addAll(currLine);
        nodes.add(new InsnNode(Instruction.NL));
        currLine.clear();
        rule = null;
    }

    public void add(InsnNode node) throws QtfParseException {
        if ((node = verifyNode(node)) != null) {
            currLine.add(node);
        }
    }

    public void add(int instruction) throws QtfParseException {
        add(new InsnNode(instruction));
    }

    private InsnNode verifyNode(InsnNode next) throws QtfParseException {
        if (rule == null || validator == null) {
            recalculateSyntax(next);
        }
        return validator.verifyNode(this, next);
    }

    public int lineSize() {
        return currLine.size();
    }

    public InsnNode getLast() {
        return currLine.isEmpty() ? null : currLine.getLast();
    }

    public InsnNode lookBack(int amount) {
        if (amount == 1) {
            return getLast();
        }
        int i = currLine.size() - amount;
        return i >= 0 ? currLine.get(i) : null;
    }

    public boolean wasLast(Predicate<Integer> pred) {
        return !currLine.isEmpty() && pred.test(currLine.getLast().instruction);
    }

    public boolean wasLast(int instruction) {
        return !currLine.isEmpty() && currLine.getLast().instruction == instruction;
    }

    public void removeLast() {
        currLine.removeLast();
    }

    public InterpretedScript build() throws QtfParseException {
        newLine();
        for (int i = 0; i < nodes.size(); ++i) {
            nodes.get(i).index = i;
        }
        return new InterpretedScript(nodes, state.compile(), state);
    }

    @Override
    public Iterator<Interpreter> iterator() {
        if (rule == null) {
            return parser.syntax.interpreters().iterator();
        }
        return rule.interpreters().iterator();
    }

    public record InterpretedScript(List<InsnNode> nodes, MemberMap members, InterpreterState state) {
    }
}
