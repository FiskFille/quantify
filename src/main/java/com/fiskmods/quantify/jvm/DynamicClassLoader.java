package com.fiskmods.quantify.jvm;

public class DynamicClassLoader extends ClassLoader {
    public DynamicClassLoader() {
        /*
         * Ensure that dynamically generated classes can always be cast to
         * JvmRunnable, regardless of how it was loaded.
         */
        super(JvmRunnable.class.getClassLoader());
    }

    public Class<?> defineClass(String name, byte[] b) throws ClassFormatError {
        return defineClass(name, b, 0, b.length);
    }

    @Override
    public String toString() {
        return "QTF-JVM-BRIDGE";
    }
}
