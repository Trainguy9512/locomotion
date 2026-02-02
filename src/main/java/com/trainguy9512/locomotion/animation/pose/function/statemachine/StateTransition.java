package com.trainguy9512.locomotion.animation.pose.function.statemachine;

import com.trainguy9512.locomotion.animation.data.PoseTickEvaluationContext;
import com.trainguy9512.locomotion.animation.driver.Driver;
import com.trainguy9512.locomotion.animation.driver.DriverKey;
import com.trainguy9512.locomotion.animation.pose.function.AnimationPlayer;
import com.trainguy9512.locomotion.animation.util.TimeSpan;
import com.trainguy9512.locomotion.animation.util.Transition;
import net.minecraft.util.Tuple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Predicate;

public record StateTransition(
        String target,
        Predicate<StateTransitionContext> conditionPredicate,
        Transition transition,
        int priority,
        Consumer<PoseTickEvaluationContext> onTransitionTakenListener,
        boolean isAutomaticTransition
) implements Comparable<StateTransition> {

    private static final Logger LOGGER = LogManager.getLogger("Locomotion/StateTransition");

    public static final Predicate<StateTransitionContext> ALWAYS_TRUE = transitionContext -> true;
    public static final Predicate<StateTransitionContext> CURRENT_TRANSITION_FINISHED = transitionContext -> transitionContext.currentStateWeight() == 1 && transitionContext.previousStateWeight() == 1;
    public static final Predicate<StateTransitionContext> MOST_RELEVANT_ANIMATION_PLAYER_IS_FINISHING = context -> hasMostRelevantAnimationPlayerFinished(context, 1f);
    public static final Predicate<StateTransitionContext> MOST_RELEVANT_ANIMATION_PLAYER_HAS_FINISHED = context -> hasMostRelevantAnimationPlayerFinished(context, 0f);

    public static <D extends Driver<Boolean>> Predicate<StateTransitionContext> takeIfBooleanDriverTrue(DriverKey<D> booleanDriverKey) {
        return transitionContext -> transitionContext.getDriverValue(booleanDriverKey);
    }

    public static Predicate<StateTransitionContext> takeIfTimeInStateLessThan(TimeSpan time) {
        return context -> context.timeElapsedInCurrentState().inTicks() < time.inTicks();
    }

    public static Predicate<StateTransitionContext> takeIfTimeInStateGreaterThan(TimeSpan time) {
        return context -> context.timeElapsedInCurrentState().inTicks() > time.inTicks();
    }

    public static boolean hasMostRelevantAnimationPlayerFinished(StateTransitionContext context, float crossFadeWeight) {
        var potentialPlayer = context.findMostRelevantAnimationPlayer();
        if (potentialPlayer.isPresent()) {
            AnimationPlayer player = potentialPlayer.get();
            float transitionTimeTicks = context.transitionDuration().inTicks() * crossFadeWeight;
            Tuple<TimeSpan, TimeSpan> remainingTime = player.getRemainingTime();

            // Mid-animation
            if (remainingTime.getA().inTicks() > remainingTime.getB().inTicks()) {
                return transitionTimeTicks < remainingTime.getA().inTicks() && transitionTimeTicks >= remainingTime.getB().inTicks();
                // Looping (remaining time wrapping around 0), but NOT stopped.
            } else if (remainingTime.getA().inTicks() < remainingTime.getB().inTicks()) {
                return transitionTimeTicks < remainingTime.getA().inTicks();
            }
        }
        return false;
    }

    /**
     * Creates a new state transition builder with the provided state identifier as the target.
     *
     * @param target Destination state identifier of the transition
     */
    public static Builder builder(String target) {
        return new Builder(target);
    }

    @Override
    public int compareTo(@NotNull StateTransition other) {
        return Integer.compare(other.priority(), this.priority());
    }

    public static class Builder {
        private final String target;
        private Predicate<StateTransitionContext> conditionPredicate;
        private Transition transition;
        private int priority;
        private Consumer<PoseTickEvaluationContext> onTransitionTakenListener;
        private boolean canInterruptOtherTransitions;
        private boolean automaticTransition;
        private float automaticTransitionCrossfadeWeight;

        private Builder(String target) {
            this.conditionPredicate = null;
            this.target = target;
            this.transition = Transition.SINGLE_TICK;
            this.priority = 50;
            this.onTransitionTakenListener = context -> {};
            this.canInterruptOtherTransitions = true;
            this.automaticTransition = false;
            this.automaticTransitionCrossfadeWeight = 1f;
        }

        /**
         * Determines whether this transition can be taken if other transitions are still active.
         * <p>
         * Useful for states where you can have a lot of back and forth with resetting states that are faster than the transitions themselves.
         *
         * @param canInterruptOtherTransitions If true, this transition will not be taken if another transition is still in progress.
         */
        public Builder setCanInterruptOtherTransitions(boolean canInterruptOtherTransitions) {
            this.canInterruptOtherTransitions = canInterruptOtherTransitions;
            return this;
        }

        /**
         * Sets the transition to be passable as an OR condition if the most relevant
         * animation player is within the transition duration of finishing.
         * <p>
         * In other words, if the sequence player in the current active state loops or ends,
         * this becomes true.
         *
         * @param crossFadeWeight Weight of how the transition duration affects the condition. 1 = Full crossfade, 0 = Always at end of animation
         */
        public Builder isTakenOnAnimationFinished(float crossFadeWeight) {
            this.automaticTransition = true;
            this.automaticTransitionCrossfadeWeight = crossFadeWeight;
            return this;
        }

        /**
         * Sets the condition predicate that determines whether the transition will be taken or not.
         *
         * <p>For "AND" or "OR" conditions, use {@link Predicate#and(Predicate)} or {@link Predicate#or(Predicate)}.</p>
         *
         * @param conditionPredicate Function that returns true or false based on the transition context.
         */
        public final Builder isTakenIfTrue(Predicate<StateTransitionContext> conditionPredicate) {
            this.conditionPredicate = conditionPredicate;
            return this;
        }

        /**
         * Sets the transition timing properties for the state transition. Default is a single-tick linear transition.
         *
         * @param transition The {@link Transition} to use
         */
        public Builder setTiming(Transition transition) {
            this.transition = transition;
            return this;
        }

        /**
         * Sets the transition priority for the state transition, for when more than one transition is active on the same tick.
         *
         * <p>Higher integers specify a higher priority. If more than one transition has the same priority, then it is picked at random.</p>
         *
         * <p>Default priority is <code>50</code>.</p>
         *
         * @param priority Priority integer
         */
        public Builder setPriority(int priority) {
            this.priority = priority;
            return this;
        }

        /**
         * Binds an event to be called every time this transition is entered in the state machine.
         *
         * <p>Multiple events can be chained together with multiple calls to this method.</p>
         */
        public Builder bindToOnTransitionTaken(Consumer<PoseTickEvaluationContext> onTransitionTaken) {
            this.onTransitionTakenListener = this.onTransitionTakenListener.andThen(onTransitionTaken);
            return this;
        }

        public StateTransition build() {
            if (this.conditionPredicate == null) {
                this.conditionPredicate = context -> false;
                if (!this.automaticTransition) {
                    LOGGER.warn("State transition to target {}.{} has no passable conditions, and will go unused.", this.target.getClass().getSimpleName(), this.target);
                }
            }
            if (!this.canInterruptOtherTransitions) {
                this.conditionPredicate = this.conditionPredicate.and(StateTransition.CURRENT_TRANSITION_FINISHED);
            }
            if (this.automaticTransition) {
                this.conditionPredicate = this.conditionPredicate.or(context -> hasMostRelevantAnimationPlayerFinished(context, this.automaticTransitionCrossfadeWeight));
            }
            return new StateTransition(this.target, this.conditionPredicate, this.transition, this.priority, this.onTransitionTakenListener, this.automaticTransition);
        }
    }

}
