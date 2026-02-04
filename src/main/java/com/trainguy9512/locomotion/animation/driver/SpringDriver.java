package com.trainguy9512.locomotion.animation.driver;

import com.trainguy9512.locomotion.animation.util.Interpolator;
import org.joml.Vector3f;

import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;

public class SpringDriver<D> extends VariableDriver<D> {

    private final float stiffness;
    private final float damping;
    private final float mass;

    private final BinaryOperator<D> addition;
    private final BiFunction<D, Float, D> multiplication;
    private final boolean returnsDelta;

    private D currentTargetValue;
    private D previousTargetValue;
    private D velocity;

    protected SpringDriver(
            float stiffness,
            float damping,
            float mass,
            Supplier<D> initialValue,
            Interpolator<D> interpolator,
            BinaryOperator<D> addition,
            BiFunction<D, Float, D> multiplication,
            boolean returnsDelta
    ) {
        super(initialValue, interpolator);
        this.stiffness = stiffness;
        this.damping = damping;
        this.mass = Math.max(mass, 0.1f);

        this.addition = addition;
        this.multiplication = multiplication;
        this.returnsDelta = returnsDelta;

        this.currentTargetValue = initialValue.get();
        this.previousTargetValue = initialValue.get();
        this.velocity = multiplication.apply(initialValue.get(), 0f);
    }

    public static <D> SpringDriver<D> of(float stiffness, float damping, float mass, Supplier<D> initialValue, Interpolator<D> interpolator, BinaryOperator<D> addition, BiFunction<D, Float, D> multiplication, boolean returnsDelta) {
        return new SpringDriver<>(stiffness, damping, mass, initialValue, interpolator, addition, multiplication, returnsDelta);
    }

    public static SpringDriver<Float> ofFloat(float stiffness, float damping, float mass, Supplier<Float> initialValue, boolean returnsDelta) {
        return SpringDriver.of(stiffness, damping, mass, initialValue,
                Interpolator.FLOAT,
                Float::sum,
                (a, b) -> a * b,
                returnsDelta
        );
    }

    public static SpringDriver<Vector3f> ofVector3f(float stiffness, float damping, float mass, Supplier<Vector3f> initialValue, boolean returnsDelta) {
        return SpringDriver.of(stiffness, damping, mass, initialValue,
                Interpolator.VECTOR_FLOAT,
                (a, b) -> a.add(b, new Vector3f()),
                (a, b) -> a.mul(b, new Vector3f()),
                returnsDelta
        );
    }

    @Override
    public void setValue(D value) {
        this.currentTargetValue = value;
    }

    @Override
    public void pushCurrentToPrevious() {
        super.pushCurrentToPrevious();
        this.previousTargetValue = this.currentTargetValue;
    }

    @Override
    public void reset() {
        super.reset();
        this.previousTargetValue = this.initialValue.get();
        this.currentTargetValue = this.initialValue.get();
    }

    @Override
    public D getInterpolatedValue(float partialTicks) {
        D interpolatedValue = super.getInterpolatedValue(partialTicks);
        if (this.returnsDelta) {
            D targetInterpolatedValue = this.interpolator.interpolate(this.previousTargetValue, this.currentTargetValue, partialTicks);
            return this.addition.apply(targetInterpolatedValue, this.multiplication.apply(interpolatedValue, -1f));
        } else {
            return interpolatedValue;
        }
    }

    @Override
    public void tick() {
        super.tick();

        D displacement = this.addition.apply(this.currentValue, this.multiplication.apply(this.currentTargetValue, -1f));
        D springForce = this.multiplication.apply(displacement, this.stiffness * -1f);
        D dampingForce = this.multiplication.apply(this.velocity, this.damping * -1f);
        D acceleration = this.multiplication.apply(this.addition.apply(springForce, dampingForce), 1 / Math.max(this.mass, 0.01f));

        this.velocity = this.addition.apply(this.velocity, acceleration);

        this.currentValue = this.addition.apply(this.currentValue, this.velocity);
    }
}
