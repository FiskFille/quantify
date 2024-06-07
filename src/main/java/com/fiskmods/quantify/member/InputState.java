package com.fiskmods.quantify.member;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InputState {
    private final List<Entry> indices = new ArrayList<>();
    private int maxIndex = -1;

    public void add(int index, int varId) {
        indices.add(new Entry(index, varId));
        if (maxIndex < index) {
            maxIndex = index;
        }
    }

    public boolean containsIndex(int index) {
        return maxIndex >= index && indices.stream()
                .anyMatch(t -> t.index == index);
    }

    public int[] toArray() {
        int[] array = new int[maxIndex + 1];
        Arrays.fill(array, -1);
        for (Entry e : indices) {
            array[e.index] = e.varId;
        }
        return array;
    }

    private record Entry(int index, int varId) {
    }
}
