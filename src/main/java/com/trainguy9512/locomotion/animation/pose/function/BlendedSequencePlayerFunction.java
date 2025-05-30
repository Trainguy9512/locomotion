package com.trainguy9512.locomotion.animation.pose.function;

import com.trainguy9512.locomotion.animation.driver.Driver;
import com.trainguy9512.locomotion.animation.driver.DriverKey;
import com.trainguy9512.locomotion.animation.driver.VariableDriver;
import com.trainguy9512.locomotion.animation.pose.LocalSpacePose;
import com.trainguy9512.locomotion.animation.sequence.AnimationSequence;
import com.trainguy9512.locomotion.util.TimeSpan;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Function;

public class BlendedSequencePlayerFunction extends TimeBasedPoseFunction<LocalSpacePose> {

    private final TreeMap<Float, BlendSpace1DEntry> blendSpaceEntries;
    private final Function<FunctionEvaluationState, Float> blendPositionFunction;
    private final VariableDriver<Float> blendPosition;

    private BlendedSequencePlayerFunction(
            Function<FunctionEvaluationState, Boolean> isPlayingFunction,
            Function<FunctionEvaluationState, Float> playRateFunction,
            TimeSpan resetStartTimeOffset,
            TreeMap<Float, BlendSpace1DEntry> blendSpaceEntries,
            Function<FunctionEvaluationState, Float> blendPositionFunction
    ) {
        super(isPlayingFunction, playRateFunction, resetStartTimeOffset);
        this.blendSpaceEntries = blendSpaceEntries;
        this.blendPositionFunction = blendPositionFunction;
        this.blendPosition = VariableDriver.ofFloat(() -> 0f);
    }

    @Override
    public void tick(FunctionEvaluationState evaluationState) {
        float position = this.blendPositionFunction.apply(evaluationState);
        this.blendPosition.pushCurrentToPrevious();
        this.blendPosition.setValue(position);

        this.isPlaying = isPlayingFunction.apply(evaluationState);
        this.playRate = this.isPlaying ? playRateFunction.apply(evaluationState) * this.getPlayRateAtPosition(position) : 0;

        super.updateTime(evaluationState);
    }

    private float getPlayRateAtPosition(float position){
        var floorEntry = this.blendSpaceEntries.floorEntry(position);
        var ceilingEntry = this.blendSpaceEntries.ceilingEntry(position);

        if (floorEntry == null)
            return ceilingEntry.getValue().playRate();
        if (ceilingEntry == null)
            return floorEntry.getValue().playRate();


        // If they're both the same frame
        if (floorEntry.getKey().equals(ceilingEntry.getKey()))
            return floorEntry.getValue().playRate();

        float relativeTime = (position - floorEntry.getKey()) / (ceilingEntry.getKey() - floorEntry.getKey());
        return Mth.lerp(relativeTime, floorEntry.getValue().playRate(), ceilingEntry.getValue().playRate());
    }

    @Override
    public @NotNull LocalSpacePose compute(FunctionInterpolationContext context) {
        float interpolatedPosition = this.blendPosition.getValueInterpolated(context.partialTicks());
        TimeSpan time = this.getInterpolatedTimeElapsed(context);

        var floorEntry = this.blendSpaceEntries.floorEntry(interpolatedPosition);
        var ceilingEntry = this.blendSpaceEntries.ceilingEntry(interpolatedPosition);

        if (floorEntry == null)
            return AnimationSequence.samplePose(context.driverContainer().getJointSkeleton(), ceilingEntry.getValue().animationSequence(), time, true);
        if (ceilingEntry == null)
            return AnimationSequence.samplePose(context.driverContainer().getJointSkeleton(), floorEntry.getValue().animationSequence(), time, true);

        // If they're both the same frame
        if (floorEntry.getKey().equals(ceilingEntry.getKey()))
            return AnimationSequence.samplePose(context.driverContainer().getJointSkeleton(), floorEntry.getValue().animationSequence(), time, true);

        float relativeTime = (interpolatedPosition - floorEntry.getKey()) / (ceilingEntry.getKey() - floorEntry.getKey());
        LocalSpacePose floorPose = AnimationSequence.samplePose(context.driverContainer().getJointSkeleton(), floorEntry.getValue().animationSequence(), time, true);
        LocalSpacePose ceilingPose = AnimationSequence.samplePose(context.driverContainer().getJointSkeleton(), ceilingEntry.getValue().animationSequence(), time, true);

        return floorPose.interpolated(ceilingPose, relativeTime);
    }

    @Override
    public PoseFunction<LocalSpacePose> wrapUnique() {
        return new BlendedSequencePlayerFunction(this.isPlayingFunction, this.playRateFunction, this.resetStartTimeOffset, this.blendSpaceEntries, this.blendPositionFunction);
    }

    @Override
    public Optional<AnimationPlayer> testForMostRelevantAnimationPlayer() {
        // TODO: Revisit making blend spaces considered to be an animation player.
        return Optional.empty();
    }

    public static Builder<?> builder(Function<FunctionEvaluationState, Float> blendValueFunction) {
        return new Builder<>(blendValueFunction);
    }

    public static Builder<?> builder(DriverKey<? extends Driver<Float>> floatDriverKey) {
        return builder(evaluationState -> evaluationState.driverContainer().getDriverValue(floatDriverKey));
    }

    private record BlendSpace1DEntry(ResourceLocation animationSequence, float playRate) {

    }

    public static class Builder<B extends Builder<B>> extends TimeBasedPoseFunction.Builder<B> {

        private final TreeMap<Float, BlendSpace1DEntry> blendSpaceEntries;
        private final Function<FunctionEvaluationState, Float> blendValueFunction;


        private Builder(Function<FunctionEvaluationState, Float> blendValueFunction) {
            this.blendSpaceEntries = new TreeMap<>();
            this.blendValueFunction = blendValueFunction;
        }

        @SuppressWarnings("unchecked")
        public B addEntry(float position, ResourceLocation animationSequence, float playRate) {
            this.blendSpaceEntries.put(position, new BlendSpace1DEntry(animationSequence, playRate));
            return (B) this;
        }

        public B addEntry(float position, ResourceLocation animationSequence) {
            return this.addEntry(position, animationSequence, 1);
        }

        public BlendedSequencePlayerFunction build() {
            if (this.blendSpaceEntries.isEmpty()) {
                throw new IllegalArgumentException("Blend space player has no added entries.");
            }
            return new BlendedSequencePlayerFunction(this.isPlayingFunction, this.playRateFunction, this.resetStartTimeOffsetTicks, this.blendSpaceEntries, this.blendValueFunction);
        }
    }
}
