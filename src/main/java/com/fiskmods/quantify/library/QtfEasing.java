package com.fiskmods.quantify.library;

public interface QtfEasing {
    QtfLibrary LIBRARY = StandardQtfLibrary.builder()
            .addFunction("eiSine", QtfEasing::eiSine)
            .addFunction("eoSine", QtfEasing::eoSine)
            .addFunction("eioSine", QtfEasing::eioSine)
            .addFunction("eiQuad", QtfEasing::eiQuad)
            .addFunction("eoQuad", QtfEasing::eoQuad)
            .addFunction("eioQuad", QtfEasing::eioQuad)
            .addFunction("eiCubic", QtfEasing::eiCubic)
            .addFunction("eoCubic", QtfEasing::eoCubic)
            .addFunction("eioCubic", QtfEasing::eioCubic)
            .addFunction("eiQuart", QtfEasing::eiQuart)
            .addFunction("eoQuart", QtfEasing::eoQuart)
            .addFunction("eioQuart", QtfEasing::eioQuart)
            .addFunction("eiQuint", QtfEasing::eiQuint)
            .addFunction("eoQuint", QtfEasing::eoQuint)
            .addFunction("eioQuint", QtfEasing::eioQuint)
            .addFunction("eiExpo", QtfEasing::eiExpo)
            .addFunction("eoExpo", QtfEasing::eoExpo)
            .addFunction("eioExpo", QtfEasing::eioExpo)
            .addFunction("eiCirc", QtfEasing::eiCirc)
            .addFunction("eoCirc", QtfEasing::eoCirc)
            .addFunction("eioCirc", QtfEasing::eioCirc)
            .addFunction("eiBack", QtfEasing::eiBack)
            .addFunction("eoBack", QtfEasing::eoBack)
            .addFunction("eioBack", QtfEasing::eioBack)
            .addFunction("eiElastic", QtfEasing::eiElastic)
            .addFunction("eoElastic", QtfEasing::eoElastic)
            .addFunction("eioElastic", QtfEasing::eioElastic)
            .addFunction("eiBounce", QtfEasing::eiBounce)
            .addFunction("eoBounce", QtfEasing::eoBounce)
            .addFunction("eioBounce", QtfEasing::eioBounce)
            .build("lang/Easing");

    static double eiSine(double x) {
        return 1 - Math.cos((x * Math.PI) / 2);
    }

    static double eoSine(double x) {
        return Math.sin((x * Math.PI) / 2);
    }

    static double eioSine(double x) {
        return -(Math.cos(Math.PI * x) - 1) / 2;
    }

    static double eiQuad(double x) {
        return x * x;
    }

    static double eoQuad(double x) {
        return 1 - (1 - x) * (1 - x);
    }

    static double eioQuad(double x) {
        return x < 0.5 ? 2 * x * x : 1 - Math.pow(-2 * x + 2, 2) / 2;
    }

    static double eiCubic(double x) {
        return x * x * x;
    }

    static double eoCubic(double x) {
        return 1 - Math.pow(1 - x, 3);
    }

    static double eioCubic(double x) {
        return x < 0.5 ? 4 * x * x * x : 1 - Math.pow(-2 * x + 2, 3) / 2;
    }

    static double eiQuart(double x) {
        return x * x * x * x;
    }

    static double eoQuart(double x) {
        return 1 - Math.pow(1 - x, 4);
    }

    static double eioQuart(double x) {
        return x < 0.5 ? 8 * x * x * x * x : 1 - Math.pow(-2 * x + 2, 4) / 2;
    }

    static double eiQuint(double x) {
        return x * x * x * x * x;
    }

    static double eoQuint(double x) {
        return 1 - Math.pow(1 - x, 5);
    }

    static double eioQuint(double x) {
        return x < 0.5 ? 16 * x * x * x * x * x : 1 - Math.pow(-2 * x + 2, 5) / 2;
    }

    static double eiExpo(double x) {
        return x == 0 ? 0 : Math.pow(2, 10 * x - 10);
    }

    static double eoExpo(double x) {
        return x == 1 ? 1 : 1 - Math.pow(2, -10 * x);
    }

    static double eioExpo(double x) {
        return x == 0 ? 0
                : x == 1 ? 1
                : x < 0.5 ? Math.pow(2, 20 * x - 10) / 2
                : (2 - Math.pow(2, -20 * x + 10)) / 2;
    }

    static double eiCirc(double x) {
        return 1 - Math.sqrt(1 - Math.pow(x, 2));
    }

    static double eoCirc(double x) {
        return Math.sqrt(1 - Math.pow(x - 1, 2));
    }

    static double eioCirc(double x) {
        return x < 0.5
                ? (1 - Math.sqrt(1 - Math.pow(2 * x, 2))) / 2
                : (Math.sqrt(1 - Math.pow(-2 * x + 2, 2)) + 1) / 2;
    }

    static double eiBack(double x) {
        final double c1 = 1.70158;
        final double c3 = c1 + 1;
        return c3 * x * x * x - c1 * x * x;
    }

    static double eoBack(double x) {
        final double c1 = 1.70158;
        final double c3 = c1 + 1;
        return 1 + c3 * Math.pow(x - 1, 3) + c1 * Math.pow(x - 1, 2);
    }

    static double eioBack(double x) {
        final double c1 = 1.70158;
        final double c2 = c1 * 1.525;
        return x < 0.5
                ? (Math.pow(2 * x, 2) * ((c2 + 1) * 2 * x - c2)) / 2
                : (Math.pow(2 * x - 2, 2) * ((c2 + 1) * (x * 2 - 2) + c2) + 2) / 2;
    }

    static double eiElastic(double x) {
        final double c4 = (2 * Math.PI) / 3;
        return x == 0 ? 0
                : x == 1 ? 1
                : -Math.pow(2, 10 * x - 10) * Math.sin((x * 10 - 10.75) * c4);
    }

    static double eoElastic(double x) {
        final double c4 = (2 * Math.PI) / 3;
        return x == 0 ? 0
                : x == 1 ? 1
                : Math.pow(2, -10 * x) * Math.sin((x * 10 - 0.75) * c4) + 1;
    }

    static double eioElastic(double x) {
        final double c5 = (2 * Math.PI) / 4.5;
        return x == 0 ? 0
                : x == 1 ? 1 : x < 0.5
                ? -(Math.pow(2, 20 * x - 10) * Math.sin((20 * x - 11.125) * c5)) / 2
                : (Math.pow(2, -20 * x + 10) * Math.sin((20 * x - 11.125) * c5)) / 2 + 1;
    }

    static double eiBounce(double x) {
        return 1 - eoBounce(1 - x);
    }

    static double eoBounce(double x) {
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

    static double eioBounce(double x) {
        return x < 0.5
                ? (1 - eoBounce(1 - 2 * x)) / 2
                : (1 + eoBounce(2 * x - 1)) / 2;
    }
}
