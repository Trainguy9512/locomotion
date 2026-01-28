package com.trainguy9512.locomotion.animation.util;

import com.trainguy9512.locomotion.animation.joint.skeleton.BlendProfile;

/**
 * Represents the properties of a transition, including the duration and easing function used to
 * blend between animation poses.
 *
 * <p>In most implementations of transitions, the underlying transition itself is a linear gradient from 0 to 1
 * with the easing function applied on top.</p>
 *
 * <p>The transition duration specifies how long the transition takes to complete, as a {@link TimeSpan}.
 * The transition easement defines the type of {@link Easing} function used to modify the interpolation
 * of the transition, allowing for more interesting or stylized transitions.</p>
 *
 * @param duration          The duration of the transition.
 * @param easement          The type of {@link Easing} function to apply to the transition.
 * @param blendProfile      Blend profile for the adjusting how quickly certain joints transition.
 */
public record Transition(TimeSpan duration, Easing easement, BlendProfile blendProfile) {

    public static final Transition INSTANT = Transition.builder(TimeSpan.ofTicks(1)).setEasement(Easing.INSTANT).build();
    public static final Transition SINGLE_TICK = Transition.builder(TimeSpan.ofTicks(1)).setEasement(Easing.LINEAR).build();

    /**
     * Creates a new {@link Transition.Builder} with the provided duration.
     * @param duration      The duration of the transition
     */
    public static Builder builder(TimeSpan duration) {
        return new Builder(duration);
    }

    public Transition withInverseEasing() {
        return new Transition(this.duration, Easing.invert(this.easement), this.blendProfile);
    }

    public static class Builder {

        private final TimeSpan duration;
        private Easing easement;
        private BlendProfile blendProfile;

        private Builder(TimeSpan duration) {
            this.duration = duration;
            this.easement = Easing.LINEAR;
            this.blendProfile = null;
        }

        public Builder setEasement(Easing easement) {
            this.easement = easement;
            return this;
        }

        public Builder setBlendProfile(BlendProfile blendProfile) {
            this.blendProfile = blendProfile;
            return this;
        }

        public Transition build() {
            return new Transition(
                    this.duration,
                    this.easement,
                    this.blendProfile
            );
        }
    }
}
