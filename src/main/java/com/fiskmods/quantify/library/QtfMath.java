package com.fiskmods.quantify.library;

@SuppressWarnings("unused")
public class QtfMath {
    private static final String MATH = "java/lang/Math";
    private static final String QTF_MATH = "com/fiskmods/quantify/library/QtfMath";

    public static final QtfLibrary LIBRARY = StandardQtfLibrary.builder()
            .addFunction(MATH, "sin", 1)
            .addFunction(MATH, "cos", 1)
            .addFunction(MATH, "tan", 1)
            .addFunction(MATH, "asin", 1)
            .addFunction(MATH, "acos", 1)
            .addFunction(MATH, "atan", 1)
            .addFunction(MATH, "abs", 1)
            .addFunction(MATH, "log", 1)
            .addFunction(MATH, "log10", 1)
            .addFunction(MATH, "log1p", 1)
            .addFunction(MATH, "sqrt", 1)
            .addFunction(MATH, "cbrt", 1)
            .addFunction(MATH, "signum", 1)
            .addFunction(MATH, "sinh", 1)
            .addFunction(MATH, "cosh", 1)
            .addFunction(MATH, "tanh", 1)
            .addFunction(MATH, "exp", 1)
            .addFunction(MATH, "expm1", 1)
            .addFunction(MATH, "round", 1)
            .addFunction(MATH, "floor", 1)
            .addFunction(MATH, "ceil", 1)
            .addFunction(QTF_MATH, "wrapTo180", 1)
            .addFunction(QTF_MATH, "wrapToPi", 1)

            .addFunction(MATH, "min", 2)
            .addFunction(MATH, "max", 2)
            .addFunction(MATH, "atan2", 2)
            .addFunction(MATH, "hypot", 2)
            .addFunction(QTF_MATH, "logn", 2)
            .addFunction(QTF_MATH, "root", 2)

            .addFunction(QTF_MATH, "clamp", 3)
            .addFunction(QTF_MATH, "lerp", 3)
            .addFunction(QTF_MATH, "lerpRot", 3)

            .build("lang/Math");

    public static double logn(double base, double d) {
        return Math.log(d) / Math.log(base);
    }

    public static double root(double d, double num) {
        return Math.pow(d, 1 / num);
    }

    public static double clamp(double d, double min, double max) {
        return Math.min(Math.max(d, min), max);
    }

    public static double wrapAngleTo180(double value) {
        while (value < -180) {
            value += 360;
        }
        while (value >= 180) {
            value -= 360;
        }
        return value;
    }

    public static double wrapAngleToPi(double value) {
        while (value < -Math.PI) {
            value += 2 * Math.PI;
        }
        while (value >= Math.PI) {
            value -= 2 * Math.PI;
        }
        return value;
    }

    public static double lerp(double progress, double from, double to) {
        return from + progress * (to - from);
    }

    public static double lerpRot(double progress, double from, double to) {
        return from + progress * wrapAngleToPi(to - from);
    }
}
