package com.fiskmods.quantify.member;

public interface VarReference {
    VarReference EMPTY = new VarReference() {
        @Override
        public double get() {
            return 0;
        }

        @Override
        public void set(double value) {
        }

        @Override
        public boolean isEmpty() {
            return true;
        }
    };

    double get();

    void set(double value);

    default boolean isEmpty() {
        return false;
    }
}
