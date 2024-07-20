package com.fiskmods.quantify.member;

import java.util.stream.Stream;

@FunctionalInterface
public interface QtfListener {
    QtfListener IGNORE = (resolver, output) -> { };

    void listen(Resolver resolver, Output output);

    @FunctionalInterface
    interface Resolver {
        /**
         * <p>Resolves the variable against the provided name.</p>
         * <p>Names prefixed with <code>.</code> are output variables</p>
         */
        void subscribe(Variable var, String name);

        default void subscribe(OutputTree tree, Output output) {
            tree.resolve(this, output);
        }
    }

    @FunctionalInterface
    interface Output {
        Stream<String> keys();
    }
}
