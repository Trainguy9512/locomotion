package com.trainguy9512.locomotion.animation.driver;

import com.trainguy9512.locomotion.animation.data.OnTickDriverContainer;

import java.util.function.Consumer;

/**
 * Boolean driver that can be triggered to get a one-tick "pulse". When triggered, it will automatically be un-triggered after pose function evaluation.
 */
public class TriggerDriver implements Driver<Boolean> {

    private final int triggerTickDuration;

    private int triggerCooldown;
    private boolean triggerConsumed;

    private TriggerDriver(int triggerTickDuration) {
        this.triggerTickDuration = triggerTickDuration;
        this.triggerCooldown = 0;
    }

    public static TriggerDriver of() {
        return new TriggerDriver(1);
    }

    public static TriggerDriver of(int triggerTickDuration) {
        return new TriggerDriver(Math.max(1, triggerTickDuration));
    }

    public void trigger() {
        this.triggerCooldown = this.triggerTickDuration;
        this.triggerConsumed = false;
    }

    /**
     * Runs a function if the driver has been triggered, and then resets the driver after pose function evaluation.
     * @param runnable          Function to run if triggered.
     */
    public void runIfTriggered(Runnable runnable) {
        if (this.triggerCooldown > 0 && !this.triggerConsumed) {
            runnable.run();
            this.triggerConsumed = true;
        }
    }

    public boolean hasBeenTriggered() {
        return this.triggerCooldown > 0;
    }

    @Override
    public void tick() {

    }

    @Override
    public Boolean getValueInterpolated(float partialTicks) {
        return this.hasBeenTriggered();
    }

    @Override
    public void pushCurrentToPrevious() {

    }

    @Override
    public void postTick() {
        if (this.triggerConsumed) {
            this.triggerCooldown = Math.max(triggerCooldown - 1, 0);
        }
    }

    @Override
    public String toString() {
        return this.hasBeenTriggered() ? "Triggered!" : "Waiting...";
    }
}
