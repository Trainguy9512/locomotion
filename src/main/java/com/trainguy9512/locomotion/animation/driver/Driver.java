package com.trainguy9512.locomotion.animation.driver;

/**
 * Object for calculating and storing values derived from a data reference for use during animation pose calculation.
 * @param <D>       Data Type
 * @author          James Pelter
 */
public interface Driver<D> {

    /**
     * Updates the driver based on values set during data extraction, called once per tick after data extraction and prior to pose function tick.
     */
    void tick();

    /**
     * Returns the interpolated value between the previous tick and the current tick.
     * @param partialTicks      Percentage of a tick since the previous tick.
     * @return                  Interpolated value.
     */
    D getValueInterpolated(float partialTicks);

    /**
     * Prepares the driver for the next tick after it's finished being used in the current tick, which is usually setting the current value to the previous value
     */
    void pushCurrentToPrevious();

    /**
     * Called once per tick after data extraction and after pose function tick.
     */
    void postTick();

    /**
     * Returns the chat formatted value of this driver, for debugging purposes.
     */
    String getChatFormattedString();
}
