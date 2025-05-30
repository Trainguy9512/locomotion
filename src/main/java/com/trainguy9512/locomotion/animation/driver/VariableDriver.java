package com.trainguy9512.locomotion.animation.driver;

import com.trainguy9512.locomotion.util.Interpolator;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Driver that acts as a variable that can be updated each tick and then is interpolated.
 * @param <D>
 */
public class VariableDriver<D> implements Driver<D> {

    protected final Supplier<D> initialValue;
    protected final Interpolator<D> interpolator;

    protected D currentValue;
    protected D previousValue;

    protected VariableDriver(Supplier<D> initialValue, Interpolator<D> interpolator) {
        this.initialValue = initialValue;
        this.interpolator = interpolator;

        this.currentValue = initialValue.get();
        this.previousValue = initialValue.get();
    }

    @Override
    public void tick() {

    }

    @Override
    public D getValueInterpolated(float partialTicks) {
        return interpolator.interpolate(this.previousValue, this.currentValue, partialTicks);
    }

    public D getPreviousValue() {
        return this.previousValue;
    }

    public D getCurrentValue() {
        return this.currentValue;
    }

    /**
     * Sets the value for the current tick.
     * If the new value is null, the default value is used instead.
     * @param newValue      Value to load for the current tick.
     * @implNote            Ensure that any mutable values inputted here are copies of themselves!
     */
    public void setValue(D newValue) {
        this.currentValue = newValue != null ? newValue : this.initialValue.get();
    }

    /**
     * Modifies the current value based on the provided function
     * @param valueModifier Value modifier function
     */
    public void modifyValue(Function<D, D> valueModifier) {
        this.currentValue = valueModifier.apply(this.currentValue);
    }

    /**
     * Pushes the current value to the previous tick's value.
     */
    public void pushCurrentToPrevious() {
        this.previousValue = this.currentValue;
    }

    @Override
    public void postTick() {

    }

    /**
     * Loads the driver with the driver's default value.
     */
    public void reset() {
        this.setValue(this.initialValue.get());
    }

    /**
     * Loads both the driver's current and previous values with the driver's default value
     */
    public void hardReset() {
        this.reset();
        this.pushCurrentToPrevious();
    }

    /**
     * Returns whether the value has changed since the previous tick or not.
     */
    public boolean hasValueChanged() {
        return !this.currentValue.equals(this.previousValue);
    }

    /**
     * Creates a driver of the given data type that can be interpolated between ticks.
     * @param defaultValue      Default value set from the start and set upon resetting the driver.
     * @param interpolator      Interpolation function for the data type
     */
    public static <D> VariableDriver<D> ofInterpolatable(Supplier<D> defaultValue, Interpolator<D> interpolator) {
        return new VariableDriver<>(defaultValue, interpolator);
    }

    /**
     * Creates a driver of the given data type that will pass the latest non-interpolated tick value when accessed.
     * @param defaultValue      Default value set from the start and set upon resetting the driver.
     */
    public static <D> VariableDriver<D> ofConstant(Supplier<D> defaultValue) {
        return VariableDriver.ofInterpolatable(defaultValue, Interpolator.constantBlend());
    }

    /**
     * Creates a boolean driver that will pass the latest non-interpolated tick value when accessed.
     * @param defaultValue      Default value set from the start and set upon resetting the driver.
     */
    public static VariableDriver<Boolean> ofBoolean(Supplier<Boolean> defaultValue) {
        return VariableDriver.ofInterpolatable(defaultValue, Interpolator.BOOLEAN_BLEND);
    }

    /**
     * Creates a float driver that will be linearly interpolated between ticks.
     * @param defaultValue      Default value set from the start and set upon resetting the driver.
     */
    public static VariableDriver<Float> ofFloat(@Nullable Supplier<Float> defaultValue) {
        return VariableDriver.ofInterpolatable(defaultValue, Interpolator.FLOAT);
    }

    /**
     * Creates a integer driver that will be not be interpolated between ticks.
     * @param defaultValue      Default value set from the start and set upon resetting the driver.
     */
    public static VariableDriver<Integer> ofInteger(Supplier<Integer> defaultValue) {
        return VariableDriver.ofConstant(defaultValue);
    }

    /**
     * Creates a vector driver that will be linearly interpolated between ticks.
     * @param defaultValue      Default value set from the start and set upon resetting the driver.
     */
    public static VariableDriver<Vector3f> ofVector(Supplier<Vector3f> defaultValue) {
        return VariableDriver.ofInterpolatable(defaultValue, Interpolator.VECTOR_FLOAT);
    }

    @Override
    public String toString() {
        return this.currentValue.toString();
    }
}