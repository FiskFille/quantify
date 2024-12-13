package com.fiskmods.quantify.jvm;

import org.objectweb.asm.ClassWriter;

@FunctionalInterface
public interface JvmClassComposer {
    JvmClassComposer DO_NOTHING = new JvmClassComposer() {
        @Override
        public void compose(ClassWriter cw) {
        }

        @Override
        public JvmClassComposer andThen(JvmClassComposer next) {
            return next;
        }
    };

    void compose(ClassWriter cw);

    default JvmClassComposer andThen(JvmClassComposer next) {
        return (cw) -> {
            compose(cw);
            next.compose(cw);
        };
    }
}
