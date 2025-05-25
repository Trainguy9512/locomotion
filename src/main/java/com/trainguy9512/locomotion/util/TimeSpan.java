package com.trainguy9512.locomotion.util;

import org.jetbrains.annotations.NotNull;

/**
 * Utility class for representing time in different formats such as ticks, seconds, or various frames of different frame rates.
 * @author James Pelter
 */
public class TimeSpan implements Comparable<TimeSpan> {

    public static final TimeSpan ZERO = TimeSpan.ofTicks(0);

    private final float timeInTicks;

    private TimeSpan(float timeInTicks){
        this.timeInTicks = timeInTicks;
    }

    /**
     * Creates a timespan from a time measured in ticks.
     * <p>
     * Conversion is 20 ticks to 1 second.
     * @return      Timespan
     */
    public static TimeSpan ofTicks(float timeInTicks){
        return new TimeSpan(timeInTicks);
    }

    /**
     * Creates a timespan from a time measured in seconds.
     * @return      Timespan
     */
    public static TimeSpan ofSeconds(float timeInSeconds){
        return new TimeSpan(timeInSeconds * 20f);
    }

    /**
     * Creates a timespan from a time measured in frames, at 60 frames per second.
     * @return      Timespan
     */
    public static TimeSpan of60FramesPerSecond(float timeIn60FramesPerSecond){
        return TimeSpan.ofSeconds(timeIn60FramesPerSecond / 60f);
    }

    /**
     * Creates a timespan from a time measured in frames, at 30 frames per second.
     * @return      Timespan
     */
    public static TimeSpan of30FramesPerSecond(float timeIn30FramesPerSecond){
        return TimeSpan.ofSeconds(timeIn30FramesPerSecond / 30f);
    }

    /**
     * Creates a timespan from a time measured in frames, at 24 frames per second.
     * @return      Timespan
     */
    public static TimeSpan of24FramesPerSecond(float timeIn24FramesPerSecond){
        return TimeSpan.ofSeconds(timeIn24FramesPerSecond / 24f);
    }

    /**
     * Retrieves the value of this timespan in ticks.
     * @return      Float time measured in ticks.
     */
    public float inTicks(){
        return this.timeInTicks;
    }

    /**
     * Retrieves the value of this timespan in seconds.
     * @return      Float time measured in seconds.
     */
    public float inSeconds(){
        return this.timeInTicks / 20f;
    }

    @Override
    public int compareTo(@NotNull TimeSpan timeSpan) {
        if (this.timeInTicks == timeSpan.timeInTicks){
            return 0;
        } else {
            return this.timeInTicks > timeSpan.timeInTicks ? 1 : -1;
        }
    }
}
