package com.fiskmods.quantify.library;

@SuppressWarnings("unused")
public class QtfEasing {
    private static final String EASING = "com/fiskmods/quantify/library/QtfEasing";

    public static final QtfLibrary LIBRARY = StandardQtfLibrary.builder()
            .addFunction(EASING, "eiSine", 1)
            .addFunction(EASING, "eoSine", 1)
            .addFunction(EASING, "eioSine", 1)
            .addFunction(EASING, "eiQuad", 1)
            .addFunction(EASING, "eoQuad", 1)
            .addFunction(EASING, "eioQuad", 1)
            .addFunction(EASING, "eiCubic", 1)
            .addFunction(EASING, "eoCubic", 1)
            .addFunction(EASING, "eioCubic", 1)
            .addFunction(EASING, "eiQuart", 1)
            .addFunction(EASING, "eoQuart", 1)
            .addFunction(EASING, "eioQuart", 1)
            .addFunction(EASING, "eiQuint", 1)
            .addFunction(EASING, "eoQuint", 1)
            .addFunction(EASING, "eioQuint", 1)
            .addFunction(EASING, "eiExpo", 1)
            .addFunction(EASING, "eoExpo", 1)
            .addFunction(EASING, "eioExpo", 1)
            .addFunction(EASING, "eiCirc", 1)
            .addFunction(EASING, "eoCirc", 1)
            .addFunction(EASING, "eioCirc", 1)
            .addFunction(EASING, "eiBack", 1)
            .addFunction(EASING, "eoBack", 1)
            .addFunction(EASING, "eioBack", 1)
            .addFunction(EASING, "eiElastic", 1)
            .addFunction(EASING, "eoElastic", 1)
            .addFunction(EASING, "eioElastic", 1)
            .addFunction(EASING, "eiBounce", 1)
            .addFunction(EASING, "eoBounce", 1)
            .addFunction(EASING, "eioBounce", 1)
            .build("lang/Easing");

    public static double eiSine(double x) {
        return 1 - Math.cos((x * Math.PI) / 2);
    }

    public static double eoSine(double x) {
        return Math.sin((x * Math.PI) / 2);
    }

    public static double eioSine(double x) {
        return -(Math.cos(Math.PI * x) - 1) / 2;
    }

    public static double eiQuad(double x) {
        return x * x;
    }

    public static double eoQuad(double x) {
        return 1 - (1 - x) * (1 - x);
    }

    public static double eioQuad(double x) {
        return x < 0.5 ? 2 * x * x : 1 - Math.pow(-2 * x + 2, 2) / 2;
    }

    public static double eiCubic(double x) {
        return x * x * x;
    }

    public static double eoCubic(double x) {
        return 1 - Math.pow(1 - x, 3);
    }

    public static double eioCubic(double x) {
        return x < 0.5 ? 4 * x * x * x : 1 - Math.pow(-2 * x + 2, 3) / 2;
    }

    public static double eiQuart(double x) {
        return x * x * x * x;
    }

    public static double eoQuart(double x) {
        return 1 - Math.pow(1 - x, 4);
    }

    public static double eioQuart(double x) {
        return x < 0.5 ? 8 * x * x * x * x : 1 - Math.pow(-2 * x + 2, 4) / 2;
    }

    public static double eiQuint(double x) {
        return x * x * x * x * x;
    }

    public static double eoQuint(double x) {
        return 1 - Math.pow(1 - x, 5);
    }

    public static double eioQuint(double x) {
        return x < 0.5 ? 16 * x * x * x * x * x : 1 - Math.pow(-2 * x + 2, 5) / 2;
    }

    public static double eiExpo(double x) {
        return x == 0 ? 0 : Math.pow(2, 10 * x - 10);
    }

    public static double eoExpo(double x) {
        return x == 1 ? 1 : 1 - Math.pow(2, -10 * x);
    }

    public static double eioExpo(double x) {
        return x == 0 ? 0
                : x == 1 ? 1
                : x < 0.5 ? Math.pow(2, 20 * x - 10) / 2
                : (2 - Math.pow(2, -20 * x + 10)) / 2;
    }

    public static double eiCirc(double x) {
        return 1 - Math.sqrt(1 - Math.pow(x, 2));
    }

    public static double eoCirc(double x) {
        return Math.sqrt(1 - Math.pow(x - 1, 2));
    }

    public static double eioCirc(double x) {
        return x < 0.5
                ? (1 - Math.sqrt(1 - Math.pow(2 * x, 2))) / 2
                : (Math.sqrt(1 - Math.pow(-2 * x + 2, 2)) + 1) / 2;
    }

    public static double eiBack(double x) {
        final double c1 = 1.70158;
        final double c3 = c1 + 1;
        return c3 * x * x * x - c1 * x * x;
    }

    public static double eoBack(double x) {
        final double c1 = 1.70158;
        final double c3 = c1 + 1;
        return 1 + c3 * Math.pow(x - 1, 3) + c1 * Math.pow(x - 1, 2);
    }

    public static double eioBack(double x) {
        final double c1 = 1.70158;
        final double c2 = c1 * 1.525;
        return x < 0.5
                ? (Math.pow(2 * x, 2) * ((c2 + 1) * 2 * x - c2)) / 2
                : (Math.pow(2 * x - 2, 2) * ((c2 + 1) * (x * 2 - 2) + c2) + 2) / 2;
    }

    public static double eiElastic(double x) {
        final double c4 = (2 * Math.PI) / 3;
        return x == 0 ? 0
                : x == 1 ? 1
                : -Math.pow(2, 10 * x - 10) * Math.sin((x * 10 - 10.75) * c4);
    }

    public static double eoElastic(double x) {
        final double c4 = (2 * Math.PI) / 3;
        return x == 0 ? 0
                : x == 1 ? 1
                : Math.pow(2, -10 * x) * Math.sin((x * 10 - 0.75) * c4) + 1;
    }

    public static double eioElastic(double x) {
        final double c5 = (2 * Math.PI) / 4.5;
        return x == 0 ? 0
                : x == 1 ? 1 : x < 0.5
                ? -(Math.pow(2, 20 * x - 10) * Math.sin((20 * x - 11.125) * c5)) / 2
                : (Math.pow(2, -20 * x + 10) * Math.sin((20 * x - 11.125) * c5)) / 2 + 1;
    }

    public static double eiBounce(double x) {
        return 1 - eoBounce(1 - x);
    }

    public static double eoBounce(double x) {
        final double n1 = 7.5625;
        final double d1 = 2.75;

        if (x < 1 / d1) {
            return n1 * x * x;
        } else if (x < 2 / d1) {
            return n1 * (x -= 1.5 / d1) * x + 0.75;
        } else if (x < 2.5 / d1) {
            return n1 * (x -= 2.25 / d1) * x + 0.9375;
        } else {
            return n1 * (x -= 2.625 / d1) * x + 0.984375;
        }
    }

    public static double eioBounce(double x) {
        return x < 0.5
                ? (1 - eoBounce(1 - 2 * x)) / 2
                : (1 + eoBounce(2 * x - 1)) / 2;
    }
}
