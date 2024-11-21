package com.fiskmods.quantify.jvm;

import com.fiskmods.quantify.QtfCompiler;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.*;
import java.lang.reflect.InvocationTargetException;

import static org.objectweb.asm.Opcodes.*;

public class JvmCompiler {
    private static final String RUNNABLE_SCRIPT = "com/fiskmods/quantify/jvm/JvmRunnable";

    public static ClassWriter compile(JvmFunction function, String name) {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        MethodVisitor mv;
        cw.visit(61, ACC_PUBLIC + ACC_SUPER, name, null, "java/lang/Object",
                new String[] {RUNNABLE_SCRIPT});
        cw.visitSource(name + ".java", null);
        mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        mv.visitInsn(RETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
        mv = cw.visitMethod(ACC_PUBLIC, "run", "([D[D)V", null, null);
        function.apply(mv);
        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
        cw.visitEnd();
        return cw;
    }

    public static JvmRunnable compile(JvmFunction function, String name, DynamicClassLoader classLoader)
            throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        byte[] b = compile(function, name.replace('.', '/')).toByteArray();
        if (QtfCompiler.DEBUG) {
            writeClassFile(name, b);
        }
        Class<?> c = classLoader.defineClass(name, b);
        return (JvmRunnable) c.getConstructor().newInstance();
    }

    private static void writeClassFile(String name, byte[] data) {
        try {
            File outDir = new File("debug/");
            if (!outDir.exists() && !outDir.mkdirs()) {
                return;
            }
            try (DataOutputStream out = new DataOutputStream(new FileOutputStream(new File(outDir, name + ".class")))) {
                out.write(data);
                out.flush();
            }
            try (OutputStream out = new FileOutputStream(new File(outDir, name + "_Bytecode.txt"))) {
                ClassReader reader = new ClassReader(data);
                TraceClassVisitor tcv = new TraceClassVisitor(new PrintWriter(out));
                reader.accept(tcv, 0);
                out.flush();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
