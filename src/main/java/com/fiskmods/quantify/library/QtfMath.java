package com.fiskmods.quantify.library;

public interface QtfMath {
    QtfLibrary LIBRARY = StandardQtfLibrary.builder()
            .addFunction("sin", Math::sin)
            .addFunction("cos", Math::cos)
            .addFunction("tan", Math::tan)
            .addFunction("asin", Math::asin)
            .addFunction("acos", Math::acos)
            .addFunction("atan", Math::atan)
            .addFunction("abs", Math::abs)
            .addFunction("log", Math::log)
            .addFunction("log10", Math::log10)
            .addFunction("log1p", Math::log1p)
            .addFunction("sqrt", Math::sqrt)
            .addFunction("cbrt", Math::cbrt)
            .addFunction("signum", Math::signum)
            .addFunction("sinh", Math::sinh)
            .addFunction("cosh", Math::cosh)
            .addFunction("tanh", Math::tanh)
            .addFunction("exp", Math::exp)
            .addFunction("expm1", Math::expm1)
            .addFunction("round", Math::round)
            .addFunction("floor", Math::floor)
            .addFunction("ceil", Math::ceil)
            .addFunction("wrapTo180", QtfMath::wrapAngleTo180)
            .addFunction("wrapToPi", QtfMath::wrapAngleToPi)

            .addFunction("min", Math::min)
            .addFunction("max", Math::max)
            .addFunction("atan2", Math::atan2)
            .addFunction("hypot", Math::hypot)
            .addFunction("logn", QtfMath::logn)
            .addFunction("root", QtfMath::root)

            .addFunction("clamp", QtfMath::clamp)
            .addFunction("lerp", QtfMath::lerp)
            .addFunction("lerpRot", QtfMath::lerpRot)

            .build("lang/Math");

    static double logn(double base, double d) {
        return Math.log(d) / Math.log(base);
    }

    static double root(double d, double num) {
        return Math.pow(d, 1 / num);
    }

    static double clamp(double d, double min, double max) {
        return Math.min(Math.max(d, min), max);
    }

    static double wrapAngleTo180(double value) {
        while (value < -180) {
            value += 360;
        }
        while (value >= 180) {
            value -= 360;
        }
        return value;
    }

    static double wrapAngleToPi(double value) {
        while (value < -Math.PI) {
            value += 2 * Math.PI;
        }
        while (value >= Math.PI) {
            value -= 2 * Math.PI;
        }
        return value;
    }

    static double lerp(double progress, double from, double to) {
        return from + progress * (to - from);
    }

    static double lerpRot(double progress, double from, double to) {
        return from + progress * wrapAngleToPi(to - from);
    }
}
