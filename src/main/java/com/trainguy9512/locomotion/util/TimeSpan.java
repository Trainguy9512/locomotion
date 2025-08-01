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

    public static TimeSpan ofFramesPerSecond(float frames, float framesPerSecond) {
        return new TimeSpan(20 * frames / framesPerSecond);
    }

    /**
     * Creates a timespan from a time measured in frames, at 60 frames per second.
     * @return      Timespan
     */
    public static TimeSpan of60FramesPerSecond(float frames){
        return TimeSpan.ofFramesPerSecond(frames, 60);
    }

    /**
     * Creates a timespan from a time measured in frames, at 30 frames per second.
     * @return      Timespan
     */
    public static TimeSpan of30FramesPerSecond(float frames){
        return TimeSpan.ofFramesPerSecond(frames, 30);
    }

    /**
     * Creates a timespan from a time measured in frames, at 24 frames per second.
     * @return      Timespan
     */
    public static TimeSpan of24FramesPerSecond(float frames){
        return TimeSpan.ofFramesPerSecond(frames, 24);
    }

    /**
     * Retrieves the value of this time span in ticks.
     * @return      Float time measured in ticks.
     */
    public float inTicks(){
        return this.timeInTicks;
    }

    /**
     * Retrieves the value of this time span in seconds.
     * @return      Float time measured in seconds.
     */
    public float inSeconds(){
        return this.timeInTicks / 20f;
    }

    /**
     * Retrieves the value of this time span in the specified frame rate.
     * @return      Float time measured in frames.
     */
    public float inFramesPerSecond(float framesPerSecond){
        return this.timeInTicks * framesPerSecond / 20f;
    }

    /**
     * Retrieves the value of this time span in frames at 60 frames per second.
     * @return      Float time measured in 60 frames per second.
     */
    public float in60FramesPerSecond(){
        return this.inFramesPerSecond(60);
    }

    /**
     * Retrieves the value of this time span in frames at 30 frames per second.
     * @return      Float time measured in 30 frames per second.
     */
    public float in30FramesPerSecond(){
        return this.inFramesPerSecond(30);
    }

    /**
     * Retrieves the value of this time span in frames at 24 frames per second.
     * @return      Float time measured in 24 frames per second.
     */
    public float in24FramesPerSecond(){
        return this.inFramesPerSecond(24);
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
