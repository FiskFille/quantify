package com.fiskmods.quantify.jvm;

public class DynamicClassLoader extends ClassLoader {
    public Class<?> defineClass(String name, byte[] b) throws ClassFormatError {
        return defineClass(name, b, 0, b.length);
    }
}
