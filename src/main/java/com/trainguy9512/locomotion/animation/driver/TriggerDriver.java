package com.trainguy9512.locomotion.animation.driver;

/**
 * Boolean driver that can be triggered to get a one-tick "pulse". When triggered, it will automatically be un-triggered after pose function evaluation.
 */
public class TriggerDriver implements Driver<Boolean> {

    private boolean triggered;

    private TriggerDriver() {
        this.triggered = false;
    }

    public static TriggerDriver of() {
        return new TriggerDriver();
    }

    public void trigger() {
        this.triggered = true;
    }

    public void ifTriggered(Runnable runnable) {
        if (this.triggered) {
            runnable.run();
        }
    }

    @Override
    public void tick() {

    }

    @Override
    public Boolean getValueInterpolated(float partialTicks) {
        return this.triggered;
    }

    @Override
    public void pushCurrentToPrevious() {

    }

    @Override
    public void postTick() {
        this.triggered = false;
    }

    @Override
    public String toString() {
        return this.triggered ? "Triggered!" : "Waiting...";
    }
}
