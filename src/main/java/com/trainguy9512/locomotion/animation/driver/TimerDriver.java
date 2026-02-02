package com.trainguy9512.locomotion.animation.driver;

import com.trainguy9512.locomotion.animation.util.Easing;
import com.trainguy9512.locomotion.animation.util.Interpolator;
import com.trainguy9512.locomotion.animation.util.TimeSpan;

import java.util.function.Supplier;

public class TimerDriver extends VariableDriver<Float> {

    private float incrementPerTick;
    private Easing easing;
    private final float minValue;
    private final float maxValue;

    protected TimerDriver(
            Supplier<Float> initialValue,
            float incrementPerTick,
            Easing easing,
            float minValue,
            float maxValue
    ) {
        super(initialValue, Interpolator.FLOAT);
        this.incrementPerTick = incrementPerTick;
        this.easing = easing;
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    public static Builder builder(Supplier<Float> initialValue) {
        return new Builder(initialValue);
    }

    public void setIncrementPerTick(float incrementPerTick) {
        this.incrementPerTick = incrementPerTick;
    }

    public void setIncrementPerTick(TimeSpan timeToIncrementFully, float target) {
        this.incrementPerTick = 1 / target / timeToIncrementFully.inTicks();
    }

    @Override
    public Float getInterpolatedValue(float partialTicks) {
        return this.easing.ease(super.getInterpolatedValue(partialTicks));
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
        private Easing easing;
        private float minValue;
        private float maxValue;

        private Builder(Supplier<Float> initialValue) {
            this.initialValue = initialValue;
            this.incrementPerTick = 1f;
            this.easing = Easing.LINEAR;
            this.minValue = -Float.MAX_VALUE;
            this.maxValue = Float.MAX_VALUE;
        }

        public Builder setInitialIncrement(float incrementPerTick) {
            this.incrementPerTick = incrementPerTick;
            return this;
        }

        public Builder setEasing(Easing easing) {
            this.easing = easing;
            return this;
        }

        public Builder setMinValue(float minValue) {
            this.minValue = minValue;
            return this;
        }

        public Builder setMaxValue(float maxValue) {
            this.maxValue = maxValue;
            return this;
        }

        public TimerDriver build() {
            return new TimerDriver(
                    this.initialValue,
                    this.incrementPerTick,
                    this.easing,
                    this.minValue,
                    this.maxValue
            );
        }
    }
}