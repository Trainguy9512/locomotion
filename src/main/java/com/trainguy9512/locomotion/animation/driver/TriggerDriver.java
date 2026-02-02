package com.trainguy9512.locomotion.animation.driver;

import net.minecraft.ChatFormatting;

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
        this.triggerConsumed = false;
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
    public void runAndConsumeIfTriggered(Runnable runnable) {
        if (this.hasBeenTriggeredAndNotConsumed()) {
            runnable.run();
            this.consume();
        }
    }

    public void consume() {
        this.triggerConsumed = true;
    }

    public boolean hasBeenConsumed() {
        return this.triggerConsumed;
    }

    public boolean hasBeenTriggered() {
        return this.triggerCooldown > 0;
    }

    public boolean hasBeenTriggeredAndNotConsumed() {
        return this.hasBeenTriggered() && !this.hasBeenConsumed();
    }

    @Override
    public void tick() {

    }

    @Override
    public Boolean getInterpolatedValue(float partialTicks) {
        return this.getCurrentValue();
    }

    @Override
    public Boolean getCurrentValue() {
        return this.hasBeenTriggered();
    }

    @Override
    public void pushCurrentToPrevious() {

    }

    @Override
    public void postTick() {
        if (this.triggerConsumed && this.triggerCooldown > 0) {
            this.triggerCooldown -= 1;
        }
    }

    @Override
    public String toString() {
        return this.hasBeenTriggered() ? "Triggered!" : "Waiting...";
    }

    @Override
    public String getChatFormattedString() {
        return (this.hasBeenTriggered() ? ChatFormatting.LIGHT_PURPLE : ChatFormatting.BLUE) + this.toString();
    }
}
