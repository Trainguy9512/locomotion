package com.trainguy9512.locomotion.animation.pose.function;

import com.google.common.collect.Maps;
import com.trainguy9512.locomotion.animation.sequence.AnimationSequence;
import com.trainguy9512.locomotion.animation.joint.JointChannel;
import com.trainguy9512.locomotion.animation.pose.LocalSpacePose;
import com.trainguy9512.locomotion.resource.LocomotionResources;
import com.trainguy9512.locomotion.util.TimeSpan;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.Tuple;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class SequencePlayerFunction extends TimeBasedPoseFunction<LocalSpacePose> implements AnimationPlayer {

    private final Identifier animationSequence;
    private final boolean isLooping;
    private final boolean ignoredByRelevancyTest;
    private final Map<String, Consumer<FunctionEvaluationState>> timeMarkerBindings;
    private final boolean isAdditive;
    private final SequenceReferencePoint additiveSubtractionReferencePoint;

    private LocalSpacePose additiveSubtractionPose;

    protected SequencePlayerFunction(
            Function<FunctionEvaluationState, Boolean> isPlayingFunction,
            Function<FunctionEvaluationState, Float> playRateFunction,
            TimeSpan resetStartTimeOffset,
            Identifier animationSequence,
            boolean isLooping,
            boolean ignoredByRelevancyTest,
            Map<String, Consumer<FunctionEvaluationState>> timeMarkerBindings,
            boolean isAdditive,
            SequenceReferencePoint additiveSubtractionReferencePoint
    ) {
        super(isPlayingFunction, playRateFunction, resetStartTimeOffset);
        this.animationSequence = animationSequence;
        this.isLooping = isLooping;
        this.timeMarkerBindings = timeMarkerBindings;
        this.additiveSubtractionReferencePoint = additiveSubtractionReferencePoint;
        this.ignoredByRelevancyTest = false;
        this.isAdditive = isAdditive;
    }

    @Override
    public @NotNull LocalSpacePose compute(FunctionInterpolationContext context) {
        LocalSpacePose pose = AnimationSequence.samplePose(
                context.driverContainer().getJointSkeleton(),
                this.animationSequence,
                this.getInterpolatedTimeElapsed(context),
                this.isLooping
        );
        AnimationSequence sequence = LocomotionResources.getOrThrowAnimationSequence(this.animationSequence);

        if (!this.isAdditive) {
            return pose;
        }
        if (this.additiveSubtractionPose == null) {
            this.additiveSubtractionPose = AnimationSequence.samplePose(
                    context.driverContainer().getJointSkeleton(),
                    this.animationSequence,
                    this.resetStartTimeOffset.interpolated(sequence.length(), this.additiveSubtractionReferencePoint.getProgressThroughSequence()),
                    false
            );
            this.additiveSubtractionPose.invert();
        }
        pose.multiply(this.additiveSubtractionPose, JointChannel.TransformSpace.COMPONENT);
        return pose;
    }

    @Override
    public void tick(FunctionEvaluationState evaluationState) {
        super.tick(evaluationState);
        Set<String> timeMarkersToFire = LocomotionResources.getOrThrowAnimationSequence(this.animationSequence).getMarkersInRange(TimeSpan.ofTicks(this.ticksElapsed.getCurrentValue()), TimeSpan.ofTicks(this.ticksElapsed.getCurrentValue() + this.playRate), this.isLooping);
        for (String timeMarker : timeMarkersToFire) {
            if (this.timeMarkerBindings.containsKey(timeMarker)) {
                this.timeMarkerBindings.get(timeMarker).accept(evaluationState);
            }
        }
    }

    @Override
    public PoseFunction<LocalSpacePose> wrapUnique() {
        return new SequencePlayerFunction(
                this.isPlayingFunction,
                this.playRateFunction,
                this.resetStartTimeOffset,
                this.animationSequence,
                this.isLooping,
                this.ignoredByRelevancyTest,
                this.timeMarkerBindings,
                this.isAdditive,
                this.additiveSubtractionReferencePoint
        );
    }

    @Override
    public Optional<PoseFunction<?>> searchDownChainForMostRelevant(Predicate<PoseFunction<?>> findCondition) {
        return findCondition.test(this) && !this.ignoredByRelevancyTest ? Optional.of(this) : Optional.empty();
    }

    public static Builder<?> builder(Identifier animationSequence) {
        return new Builder<>(animationSequence);
    }

    @Override
    public Tuple<TimeSpan, TimeSpan> getRemainingTime() {
        float lengthInTicks = LocomotionResources.getOrThrowAnimationSequence(animationSequence).length().inTicks();
        float remainingTimePreviously;
        float remainingTimeCurrently;
        if (this.isLooping) {
            remainingTimePreviously = lengthInTicks - ((this.ticksElapsed.getCurrentValue() - this.playRate) % lengthInTicks);
            remainingTimeCurrently = lengthInTicks - (this.ticksElapsed.getCurrentValue() % lengthInTicks);
        } else {
            remainingTimePreviously = lengthInTicks - (Mth.clamp(this.ticksElapsed.getCurrentValue() - this.playRate, 0, lengthInTicks));
            remainingTimeCurrently = lengthInTicks - (Mth.clamp(this.ticksElapsed.getCurrentValue(), 0, lengthInTicks));
        }
        return new Tuple<>(TimeSpan.ofTicks(remainingTimePreviously), TimeSpan.ofTicks(remainingTimeCurrently));
    }

    @Override
    public TimeSpan getAnimationLength() {
        return LocomotionResources.getOrThrowAnimationSequence(this.animationSequence).length();
    }

    public static class Builder<B extends Builder<B>> extends TimeBasedPoseFunction.Builder<B>{

        private final Identifier animationSequence;
        private boolean looping;
        private boolean ignoredForRelevancyTest;
        private final Map<String, Consumer<FunctionEvaluationState>> timeMarkerBindings;
        private boolean isAdditive;
        private SequenceReferencePoint additiveSubtractionReferencePoint;

        protected Builder(Identifier animationSequence) {
            super();
            this.animationSequence = animationSequence;
            this.looping = false;
            this.ignoredForRelevancyTest = false;
            this.timeMarkerBindings = Maps.newHashMap();
            this.isAdditive = false;
            this.additiveSubtractionReferencePoint = SequenceReferencePoint.BEGINNING;
        }

        /**
         * Sets the animation sequence player to loop when the end of the animation is reached.
         * @implNote                    The animation sequence will always be looped in full, the reset start time only
         *                              affects where the animation starts when reset.
         */
        @SuppressWarnings("unchecked")
        public B setLooping(boolean looping) {
            this.looping = looping;
            return (B) this;
        }

        /**
         * Binds an event to fire every time the sequence player passes a time marker of the given identifier.
         * <p>
         * Time markers can be defined by animation sequences within Maya. A time marker can have multiple
         * time points defined, so binding an event to an identifier will bind it for every instance of it
         * within the sequence.
         * <p>
         * Multiple bindings can be bound to the same time marker. When the marker is triggered, it will fire the events in
         * the sequence in which they were bound.
         * @param timeMarkerIdentifier  String identifier for the time marker, pointing to the associated time marker in the sequence file.
         * @param binding               Event to fire every time this time marker is passed when the sequence player is playing.
         */
        @SuppressWarnings("unchecked")
        public B bindToTimeMarker(String timeMarkerIdentifier, Consumer<FunctionEvaluationState> binding) {
            this.timeMarkerBindings.computeIfPresent(timeMarkerIdentifier, (identifier, existingBinding) -> existingBinding.andThen(binding));
            this.timeMarkerBindings.putIfAbsent(timeMarkerIdentifier, binding);
            return (B) this;
        }

        /**
         * Marks this sequence player function to be ignored by {@link PoseFunction#findMostRelevantDownChainWithTest()}.
         * <p>
         * If multiple sequence players of equal relevance are used by a state that has an automatic out-transition, use this
         * to exclude players that shouldn't be retrieved.
         */
        @SuppressWarnings("unchecked")
        public B ignoreForRelevancyTest() {
            this.ignoredForRelevancyTest = true;
            return (B) this;
        }

        /**
         * Sets whether the resulting pose of the sequence player is additive or not.
         *
         * @param subtractionReferencePoint         Point in the sequence to use as the additive subtraction pose.
         */
        @SuppressWarnings("unchecked")
        public B isAdditive(boolean isAdditive, SequenceReferencePoint subtractionReferencePoint) {
            this.isAdditive = isAdditive;
            this.additiveSubtractionReferencePoint = subtractionReferencePoint;
            return (B) this;
        }

        public SequencePlayerFunction build() {
            return new SequencePlayerFunction(
                    this.isPlayingFunction,
                    this.playRateFunction,
                    this.resetStartTimeOffsetTicks,
                    this.animationSequence,
                    this.looping,
                    this.ignoredForRelevancyTest,
                    this.timeMarkerBindings,
                    this.isAdditive,
                    this.additiveSubtractionReferencePoint
            );
        }
    }
}
