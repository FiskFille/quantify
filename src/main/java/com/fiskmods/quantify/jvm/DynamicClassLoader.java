package com.fiskmods.quantify.jvm;

public class DynamicClassLoader extends ClassLoader {
    private ClassLoader fallbackClassLoader;

    public DynamicClassLoader() {
        /*
         * Ensure that dynamically generated classes can always be cast to
         * JvmRunnable, regardless of how it was loaded.
         */
        super(JvmRunnable.class.getClassLoader());
    }

    /**
     * Specifies the ClassLoader to fall back on in the event that a class
     * could neither be loaded nor found.
     */
    public DynamicClassLoader setFallbackClassLoader(ClassLoader fallbackClassLoader) {
        this.fallbackClassLoader = fallbackClassLoader;
        return this;
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        if (fallbackClassLoader == null) {
            return super.loadClass(name);
        }
        try {
            return super.loadClass(name);
        }
        catch (ClassNotFoundException e) {
            return fallbackClassLoader.loadClass(name);
        }
    }

    Class<?> defineClass(String name, byte[] b) throws ClassFormatError {
        return defineClass(name, b, 0, b.length);
    }

    @Override
    public String toString() {
        return "QTF-JVM-BRIDGE";
    }
}
