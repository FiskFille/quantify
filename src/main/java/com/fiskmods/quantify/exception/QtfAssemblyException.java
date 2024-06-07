package com.fiskmods.quantify.exception;

import com.fiskmods.quantify.insn.InsnNode;

import java.util.Iterator;
import java.util.List;
import java.util.StringJoiner;

public class QtfAssemblyException extends QtfException {
    public QtfAssemblyException(String message) {
        super(message);
    }

    public static QtfAssemblyException error(String desc, int index, List<InsnNode> nodes) {
        return new QtfAssemblyException("Unexpected error at index %s: "
                .formatted(index) + desc + "\n" + createTrace(nodes, index));
    }

    public static String createTrace(List<InsnNode> nodes, int index) {
        String lineDelimiter = " | ";
        StringJoiner lines = new StringJoiner("\n");
        StringJoiner line = new StringJoiner(lineDelimiter);
        final int maxColumns = 12;
        final int maxRows = (int) Math.ceil((float) nodes.size() / maxColumns);
        int column = 0;
        int row = 0;

        String indent = "\t";
        Iterator<InsnNode> iter = nodes.iterator();
        String[][] table = new String[maxRows][maxColumns];
        int[] columnLen = new int[maxColumns];
        int i = 0;

        while (iter.hasNext()) {
            String s = iter.next().toString();
            columnLen[column] = Math.max(columnLen[column], s.length());
            table[row][column] = s;

            if (++column >= maxColumns) {
                column = 0;
                ++row;
            }
        }

        for (row = 0; row < maxRows; ++row) {
            String marker = null;

            for (column = 0; column < maxColumns; ++column) {
                String s = table[row][column];
                if (s == null) {
                    break;
                }
                StringBuilder sb = new StringBuilder(s);
                while (sb.length() < columnLen[column]) {
                    sb.append(" ");
                }

                if (i == index) {
                    StringBuilder s1 = new StringBuilder("\t");
                    while (s1.length() <= line.length()) {
                        s1.append(" ");
                    }
                    marker = s1 + (column > 0 ? "     ^" : "  ^");
                }

                line.add(sb.toString());
                ++i;
            }
            lines.add(indent + "| " + line + " |");
            line = new StringJoiner(lineDelimiter);

            if (marker != null) {
                lines.add(marker);
            }
        }

        return lines.toString();
    }
}
