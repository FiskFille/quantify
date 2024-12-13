package com.fiskmods.quantify.parser;

import java.util.ArrayList;

public class IndexedList<T> extends ArrayList<T> {
    public void put(int index, T value) {
        if (index == size()) {
            add(value);
        } else {
            set(index, value);
        }
    }
}
