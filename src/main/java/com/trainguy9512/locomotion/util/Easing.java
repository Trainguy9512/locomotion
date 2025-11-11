package com.trainguy9512.locomotion.util;

import com.trainguy9512.locomotion.LocomotionMain;
import net.minecraft.util.Mth;

/**
 * @author Marvin Schürz
 */
@FunctionalInterface
public interface Easing {

    /**
     * Takes in a time value from 0 to 1 and returns and eased version.
     * @param time Input value
     * @return Eased value
     */
    float ease(float time);

    Easing LINEAR = time -> time;
    Easing INSTANT = time -> 1;

    // Preset cubic beziers
    // https://easings.net/

    Easing SINE_IN = time -> (float) (1f - Math.cos((time * Math.PI) / 2f));
    Easing SINE_OUT = invert(SINE_IN);
    Easing SINE_IN_OUT = composeEaseInOut(SINE_IN);

    Easing QUAD_IN = time -> time * time;
    Easing QUAD_OUT = invert(QUAD_IN);
    Easing QUAD_IN_OUT = composeEaseInOut(QUAD_IN);

    Easing CUBIC_IN = time -> time * time * time;
    Easing CUBIC_OUT = invert(CUBIC_IN);
    Easing CUBIC_IN_OUT = composeEaseInOut(CUBIC_IN);

    Easing QUART_IN = time -> time * time * time * time;
    Easing QUART_OUT = invert(QUART_IN);
    Easing QUART_IN_OUT = composeEaseInOut(QUART_IN);

    Easing QUINT_IN = time -> time * time * time * time * time;
    Easing QUINT_OUT = invert(QUINT_IN);
    Easing QUINT_IN_OUT = composeEaseInOut(QUINT_IN);

    Easing EXPONENTIAL_IN = time -> time == 0 ? 0 : (float) Math.pow(2, 10 * time - 10);
    Easing EXPONENTIAL_OUT = invert(EXPONENTIAL_IN);
    Easing EXPONENTIAL_IN_OUT = composeEaseInOut(EXPONENTIAL_IN);

    Easing CIRC_IN = time -> (float) Math.sqrt(1 - Math.pow(time - 1, 2));
    Easing CIRC_OUT = invert(CIRC_IN);
    Easing CIRC_IN_OUT = composeEaseInOut(CIRC_IN);

    Easing BACK_IN = CubicBezier.of(0.36f, 0f, 0.66f, -0.56f);
    Easing BACK_OUT = invert(BACK_IN);
    Easing BACK_IN_OUT = composeEaseInOut(BACK_IN);

    Easing ELASTIC_IN = Elastic.of(10, false);
    Easing ELASTIC_OUT = Elastic.of(10, true);
    Easing ELASTIC_IN_OUT = composeEaseInOut(ELASTIC_IN);

    Easing BOUNCE_IN = Easing::bounceEaseIn;
    Easing BOUNCE_OUT = invert(BOUNCE_IN);
    Easing BOUNCE_IN_OUT = composeEaseInOut(BOUNCE_IN);

    private static float bounceEaseIn(float time) {
        float n1 = 7.5625f;
        float d1 = 2.75f;

        if (time < 1f / d1) {
            return n1 * time * time;
        } else if (time < 2f / d1) {
            return n1 * (time -= 1.5f / d1) * time + 0.75f;
        } else if (time < 2.5f / d1) {
            return n1 * (time -= 2.25f / d1) * time + 0.9375f;
        } else {
            return n1 * (time -= 2.625f / d1) * time + 0.984375f;
        }
    }

    class Elastic implements Easing {

        private final float bounceFactor;

        private Elastic(float bounceFactor) {
            this.bounceFactor = bounceFactor;
        }

        /**
         * Returns an ease-in elastic function using the given bounce factor.
         * <p>
         * Bounce factor preview graph: <a href="https://www.desmos.com/calculator/bpyu7zywur">Desmos</a>
         * @param bounceFactor      Value that controls the number of waves in the elastic shape. In the Desmos graph, this is variable "o".
         */
        public static Easing of(float bounceFactor, boolean inverted) {
            Easing elasticEasing = new Elastic(bounceFactor);
            if (inverted) {
                return invert(elasticEasing);
            }
            return elasticEasing;
        }

        @Override
        public float ease(float time) {
            if (time == 0) {
                return 0;
            }
            if (time == 1) {
                return 1;
            }
            float a = Mth.TWO_PI / 3f;
            float b = (float) -Math.pow(2, 10 * time - 10);
            float c = (this.bounceFactor * time) - this.bounceFactor - 0.75f;
            float d = (float) (b * Math.sin(c * a));
            return d;
        }
    }

    class CubicBezier implements Easing {

        float cx;
        float bx;
        float ax;

        float cy;
        float by;
        float ay;

        float startGradient;
        float endGradient;

        private CubicBezier(float p1x, float p1y, float p2x, float p2y) {
            cx = 3f * p1x;
            bx = 3f * (p2x - p1x) - cx;
            ax = 1f - cx - bx;

            cy = 3f * p1y;
            by = 3f * (p2y - p1y) - cy;
            ay = 1f - cy - by;

            if (p1x > 0)
                startGradient = p1y / p1x;
            else if (p1y == 0 && p2x > 0)
                startGradient = p2y / p2x;
            else
                startGradient = 0;

            if (p2x < 1)
                endGradient = (p2y - 1) / (p2x - 1);
            else if (p2x == 1 && p1x < 1)
                endGradient = (p1y - 1) / (p1x - 1);
            else
                endGradient = 0;
        }

        float sampleCurveX(float t) {
            return ((ax * t + bx) * t + cx) * t;
        }

        float sampleCurveY(float t) {
            return ((ay * t + by) * t + cy) * t;
        }

        float sampleCurveDerivativeX(float t) {
            return (3f * ax * t + 2f * bx) * t + cx;
        }

        // Given an x value, find a parametric value it came from.
        float solveCurveX(float x, float epsilon) {

            float t0;
            float t1;
            float t2;
            float x2;
            float d2;
            int i;

            // First try a few iterations of Newton's method -- normally very fast.
            for (t2 = x, i = 0; i < 8; i++) {
                x2 = sampleCurveX(t2) - x;
                if (Mth.abs(x2) < epsilon)
                    return t2;
                d2 = sampleCurveDerivativeX(t2);
                if (Mth.abs(d2) < 1e-6)
                    break;
                t2 = t2 - x2 / d2;
            }

            // Fall back to the bisection method for reliability.
            t0 = 0f;
            t1 = 1f;
            t2 = x;

            while (t0 < t1) {
                x2 = sampleCurveX(t2);
                if (Mth.abs(x2 - x) < epsilon)
                    return t2;
                if (x > x2)
                    t0 = t2;
                else
                    t1 = t2;
                t2 = (t1 - t0) * .5f + t0;
            }

            // Failure.
            return t2;
        }

        // Evaluates y at the given x. The epsilon parameter provides a hint as to the required
        // accuracy and is not guaranteed.
        float solve(float x, float epsilon) {
            if (x < 0f)
                return 0f + startGradient * x;
            if (x > 1f)
                return 1f + endGradient * (x - 1f);
            return sampleCurveY(solveCurveX(x, epsilon));
        }

        @Override
        public float ease(float time) {
            return solve(time, 0.01f);
        }

        /**
         * Creates a cubic bezier easing using two handle points.
         * Values from this website may be used as parameters here: <a href="https://cubic-bezier.com/">https://cubic-bezier.com</a>
         * @param point1X   X of point 1
         * @param point1Y   Y of point 1
         * @param point2X   X of point 2
         * @param point2Y   Y of point 2
         */
        public static CubicBezier of(float point1X, float point1Y, float point2X, float point2Y){
            return new CubicBezier(point1X, point1Y, point2X, point2Y);
        }
    }

    /**
     * Returns the inverse for the provided easing.
     */
    static Easing invert(Easing easing){
        return time -> 1 - easing.ease(1 - time);
    }

    /**
     * Returns the ease-in-out equivalent of the provided ease-in function.
     * @param easeIn    Ease-in function
     * @return          Ease-in-out function
     */
    static Easing composeEaseInOut(Easing easeIn){
        return time -> {
            if (time < 0.5f) {
                return easeIn.ease(time * 2f) / 2f;
            } else {
                return invert(easeIn).ease(time * 2f - 1f) / 2f + 0.5f;
            }
        };
    }
}
