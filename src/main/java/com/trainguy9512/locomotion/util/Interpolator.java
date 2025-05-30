package com.trainguy9512.locomotion.util;

import com.trainguy9512.locomotion.animation.pose.LocalSpacePose;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * @author Marvin Schürz
 */
@FunctionalInterface
public interface Interpolator<T>  {
    T interpolate(T a, T b, float time);

    static <T> Interpolator<T> constantKeyframe(){
        return (a, b, time) -> b;
    }

    static <T> Interpolator<T> constantBlend(){
        return (a, b, time) -> b;
    }

    Interpolator<Float> FLOAT = (a, b, time) -> a + (b - a) * time;
    Interpolator<Boolean> BOOLEAN_KEYFRAME = Interpolator.constantKeyframe();
    Interpolator<Boolean> BOOLEAN_BLEND = Interpolator.constantBlend();
    Interpolator<LocalSpacePose> LOCAL_SPACE_POSE = (a, b, time) -> a.interpolated(b, time, LocalSpacePose.of(a));

    Interpolator<Vector3f> VECTOR_FLOAT = (a, b, time) -> {
        if (time == 0) {
            return new Vector3f(a);
        }
        if (time == 1) {
            return new Vector3f(b);
        }
        if (a.equals(b)) {
            return new Vector3f(a);
        }
        return a.lerp(b, time, new Vector3f());
    };

    Interpolator<Quaternionf> QUATERNION = (a, b, time) -> {
        if (time == 0) {
            return new Quaternionf(a);
        }
        if (time == 1) {
            return new Quaternionf(b);
        }
        if (a.equals(b)) {
            return new Quaternionf(a);
        }
        return a.slerp(b, time, new Quaternionf());
    };
}
