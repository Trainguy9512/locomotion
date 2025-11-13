package com.trainguy9512.locomotion.animation.pose.function.montage;

import com.google.common.collect.Maps;
import com.trainguy9512.locomotion.animation.data.OnTickDriverContainer;
import com.trainguy9512.locomotion.animation.joint.skeleton.BlendMask;
import com.trainguy9512.locomotion.animation.pose.function.PoseFunction;
import com.trainguy9512.locomotion.util.TimeSpan;
import com.trainguy9512.locomotion.util.Transition;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Configuration for a triggerable animation, otherwise known as a Montage in Unreal Engine.
 *
 * @param identifier                        Identifier for this montage configuration.
 * @param slots                             List of slots that the montage will be reflected on during pose evaluation.
 * @param animationSequence                 Animation sequence to play
 * @param playRateFunction                  Function that provides the play rate every time a montage of this configuration is fired.
 * @param timeMarkerBindings                Bound function calls assigned to time markers.
 * @param blendMask                         Blend mask for determining which joints the montage will play on
 * @param transitionIn                      In transition timing.
 * @param transitionOut                     Out transition timing.
 * @param startTimeOffset                   The point in time the animation starts at when the montage is fired.
 * @param transitionOutCrossfadeWeight      How much the out transition cross-fades from the playing animation.
 * @param cooldownDuration                  The minimum amount of time allowed between firing montages of this configuration.
 * @param isAdditive                        Whether the montage is additive. If additive, the montage will subtract the start frame from the
 *                                          animation and then add it to the start frame of the provided additive base pose resource location.
 * @param additiveBasePoseProvider          Base pose provider added back to the additive animation.
 */
public record MontageConfiguration(
        String identifier,
        List<String> slots,
        ResourceLocation animationSequence,
        Function<OnTickDriverContainer, Float> playRateFunction,
        Map<String, Consumer<PoseFunction.FunctionEvaluationState>> timeMarkerBindings,
        @Nullable BlendMask blendMask,
        Transition transitionIn,
        Transition transitionOut,
        TimeSpan startTimeOffset,
        float transitionOutCrossfadeWeight,
        TimeSpan cooldownDuration,
        boolean isAdditive,
        Function<OnTickDriverContainer, ResourceLocation> additiveBasePoseProvider

) {

    public static Builder builder(String identifier, ResourceLocation animationSequence) {
        return new Builder(identifier, animationSequence);
    }

    /**
     * Makes a copy of this montage with a different identifier and slot it's being played in.
     * @param identifier            New identifier to give the montage copy.
     * @param animationSequence     Animation sequence to play as the montage.
     * @return New montage configuration builder.
     */
    public Builder makeBuilderCopy(String identifier, ResourceLocation animationSequence) {
        Builder builder = MontageConfiguration.builder(identifier, animationSequence)
                .setPlayRate(this.playRateFunction)
                .setBlendMask(this.blendMask)
                .setTransitionIn(this.transitionIn)
                .setTransitionOut(this.transitionOut)
                .setStartTimeOffset(this.startTimeOffset)
                .setTransitionOutCrossfadeWeight(this.transitionOutCrossfadeWeight)
                .setCooldownDuration(this.cooldownDuration);
        for (String slot : this.slots) {
            builder.playsInSlot(slot);
        }
        for (Map.Entry<String, Consumer<PoseFunction.FunctionEvaluationState>> entry : this.timeMarkerBindings.entrySet()) {
            builder.bindToTimeMarker(entry.getKey(), entry.getValue());
        }
        if (this.isAdditive) {
            builder.makeAdditive(this.additiveBasePoseProvider);
        }
        return builder;
    }

    public static class Builder {

        private final String identifier;
        private final ResourceLocation animationSequence;
        private final List<String> slots;
        private Function<OnTickDriverContainer, Float> playRateFunction;
        private Map<String, Consumer<PoseFunction.FunctionEvaluationState>> timeMarkerBindings;
        private BlendMask blendMask;
        private Transition transitionIn;
        private Transition transitionOut;
        private TimeSpan startTimeOffset;
        private float transitionOutCrossfadeWeight;
        private TimeSpan cooldownDuration;
        private boolean isAdditive;
        private Function<OnTickDriverContainer, ResourceLocation> additiveBasePoseProvider;


        private Builder(String identifier, ResourceLocation animationSequence) {
            this.identifier = identifier;
            this.animationSequence = animationSequence;
            this.slots = new ArrayList<>();
            this.playRateFunction = driverContainer -> 1f;
            this.timeMarkerBindings = Maps.newHashMap();
            this.blendMask = null;
            this.transitionIn = Transition.SINGLE_TICK;
            this.transitionOut = Transition.SINGLE_TICK;
            this.startTimeOffset = TimeSpan.ofSeconds(0);
            this.transitionOutCrossfadeWeight = 1f;
            this.cooldownDuration = TimeSpan.ofTicks(0);
            this.isAdditive = false;
            this.additiveBasePoseProvider = null;
        }

        /**
         * Adds a slot identifier that the animation will play in.
         * Multiple slots can be added, so that the montage is played in multiple different places.
         * @param slotIdentifier        String slot identifier
         */
        public Builder playsInSlot(String slotIdentifier) {
            this.slots.add(slotIdentifier);
            return this;
        }

        /**
         * Adds a list of slot identifiers that the animation will play in.
         * @param slotIdentifiers       String slot identifiers
         */
        public Builder playsInSlots(String... slotIdentifiers) {
            this.slots.addAll(List.of(slotIdentifiers));
            return this;
        }

        /**
         * Sets the rate at which the montage plays. The provided function is only called once each
         * time a montage is played, with the constant play rate for the whole animation decided then.
         * @param playRate              Play rate function.
         */
        public Builder setPlayRate(Function<OnTickDriverContainer, Float> playRate) {
            this.playRateFunction = playRate;
            return this;
        }

        /**
         * Sets the blend mask of this montage. This determines the weight of each joint when the animation plays
         * @param blendMask             Blend mask
         */
        public Builder setBlendMask(BlendMask blendMask) {
            this.blendMask = blendMask;
            return this;
        }

        /**
         * Sets the timing of the exit transition
         * @param transitionIn         Exit transition timing
         */
        public Builder setTransitionIn(Transition transitionIn) {
            this.transitionIn = transitionIn;
            return this;
        }

        /**
         * Sets the timing of the exit transition
         * @param transitionOut         Exit transition timing
         */
        public Builder setTransitionOut(Transition transitionOut) {
            this.transitionOut = transitionOut;
            return this;
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
        public Builder bindToTimeMarker(String timeMarkerIdentifier, Consumer<PoseFunction.FunctionEvaluationState> binding) {
            this.timeMarkerBindings.computeIfPresent(timeMarkerIdentifier, (identifier, existingBinding) -> existingBinding.andThen(binding));
            this.timeMarkerBindings.putIfAbsent(timeMarkerIdentifier, binding);
            return this;
        }

        /**
         * Offsets the time in the animation where the montage starts.
         * @param offset                Offset time
         */
        public Builder setStartTimeOffset(TimeSpan offset) {
            this.startTimeOffset = offset;
            return this;
        }

        /**
         * Adjusts the weight of how the transition duration affects the beginning of the exit transition.
         *
         * <p>At <code>1</code>, the exit transition will begin as the animation is finishing and will end at the same time as the animation.
         * At <code>0</code>, the exit transition will begin when the animation is fully finished, and end after the animation has finished.</p>
         *
         * @param weight                Crossfade weight
         */
        public Builder setTransitionOutCrossfadeWeight(float weight) {
            this.transitionOutCrossfadeWeight = weight;
            return this;
        }

        /**
         * Sets the duration of this montage's cooldown. Anytime a montage of this configuration is triggered, it will only play if
         * there is no montage with an elapsed time less than the cooldown duration.
         *
         * <p>Note: This value is scaled by the play rate of the montage. If a montage is playing at twice speed, the cooldown will be halved.</p>
         *
         * @param duration              Duration of the cooldown period
         */
        public Builder setCooldownDuration(TimeSpan duration) {
            this.cooldownDuration = duration;
            return this;
        }

        /**
         * Makes this montage configuration additive, meaning it will subtract the first frame of animation from the montage's
         * animation, making it additive, and then it will add it to the first frame of the provided base pose provider.
         *
         * @param additiveBasePoseProvider          Base pose provider, retrieved every time a montage of this configuration is fired.
         */
        public Builder makeAdditive(Function<OnTickDriverContainer, ResourceLocation> additiveBasePoseProvider) {
            this.isAdditive = true;
            this.additiveBasePoseProvider = additiveBasePoseProvider;
            return this;
        }

        public MontageConfiguration build() {
            return new MontageConfiguration(
                    this.identifier,
                    this.slots,
                    this.animationSequence,
                    this.playRateFunction,
                    this.timeMarkerBindings,
                    this.blendMask,
                    this.transitionIn,
                    this.transitionOut,
                    this.startTimeOffset,
                    this.transitionOutCrossfadeWeight,
                    this.cooldownDuration,
                    this.isAdditive,
                    this.additiveBasePoseProvider
            );
        }
    }
}
