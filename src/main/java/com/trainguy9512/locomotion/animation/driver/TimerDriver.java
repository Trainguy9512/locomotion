package com.trainguy9512.locomotion.animation.driver;

import com.trainguy9512.locomotion.util.Interpolator;

import java.util.function.Supplier;

public class TimerDriver extends VariableDriver<Float> {

    private float incrementPerTick;
    private final float minValue;
    private final float maxValue;

    protected TimerDriver(
            Supplier<Float> initialValue,
            float incrementPerTick,
            float minValue,
            float maxValue
    ) {
        super(initialValue, Interpolator.FLOAT);
        this.incrementPerTick = incrementPerTick;
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    public static Builder builder(Supplier<Float> initialValue) {
        return new Builder(initialValue);
    }

    public void setIncrementPerTick(float incrementPerTick) {
        this.incrementPerTick = incrementPerTick;
    }

    @Override
    public void setValue(Float newValue) {
        super.setValue(Math.clamp(newValue, this.minValue, this.maxValue));
    }

    @Override
    public void postTick() {
        this.setValue(this.currentValue + this.incrementPerTick);
    }

    public static class Builder {

        private final Supplier<Float> initialValue;
        private float incrementPerTick;
        private float minValue;
        private float maxValue;

        private Builder(Supplier<Float> initialValue) {
            this.initialValue = initialValue;
            this.incrementPerTick = 1f;
            this.minValue = -Float.MAX_VALUE;
            this.maxValue = Float.MAX_VALUE;
        }

        public void setInitialIncrement(float incrementPerTick) {
            this.incrementPerTick = incrementPerTick;
        }

        public void setMinValue(float minValue) {
            this.minValue = minValue;
        }

        public void setMaxValue(float maxValue) {
            this.maxValue = maxValue;
        }

        public TimerDriver build() {
            return new TimerDriver(
                    this.initialValue,
                    this.incrementPerTick,
                    this.minValue,
                    this.maxValue
            );
        }
    }
}
