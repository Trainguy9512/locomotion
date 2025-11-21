package com.trainguy9512.locomotion.animation.pose.function.statemachine;

import com.trainguy9512.locomotion.animation.data.OnTickDriverContainer;
import com.trainguy9512.locomotion.animation.driver.Driver;
import com.trainguy9512.locomotion.animation.driver.DriverKey;
import com.trainguy9512.locomotion.animation.pose.LocalSpacePose;
import com.trainguy9512.locomotion.animation.pose.function.AnimationPlayer;
import com.trainguy9512.locomotion.animation.pose.function.PoseFunction;
import com.trainguy9512.locomotion.util.TimeSpan;
import com.trainguy9512.locomotion.util.Transition;
import net.minecraft.util.Tuple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

public record StateTransition<S extends Enum<S>>(
        S target,
        Predicate<TransitionContext> conditionPredicate,
        Transition transition,
        int priority,
        Consumer<PoseFunction.FunctionEvaluationState> onTransitionTakenListener,
        boolean isAutomaticTransition
) implements Comparable<StateTransition<S>> {

    private static final Logger LOGGER = LogManager.getLogger("Locomotion/StateTransition");

    public static final Predicate<TransitionContext> ALWAYS_TRUE = transitionContext -> true;
    public static final Predicate<TransitionContext> CURRENT_TRANSITION_FINISHED = transitionContext -> transitionContext.currentStateWeight() == 1 && transitionContext.previousStateWeight() == 1;
    public static final Predicate<TransitionContext> MOST_RELEVANT_ANIMATION_PLAYER_IS_FINISHING = makeMostRelevantAnimationPlayerFinishedCondition(1f);
    public static final Predicate<TransitionContext> MOST_RELEVANT_ANIMATION_PLAYER_HAS_FINISHED = makeMostRelevantAnimationPlayerFinishedCondition(0f);

    public static <D extends Driver<Boolean>> Predicate<TransitionContext> takeIfBooleanDriverTrue(DriverKey<D> booleanDriverKey) {
        return transitionContext -> transitionContext.driverContainer.getDriverValue(booleanDriverKey);
    }

    public static Predicate<TransitionContext> makeMostRelevantAnimationPlayerFinishedCondition(float crossFadeWeight) {
        return transitionContext -> {
            var potentialPlayer = transitionContext.findMostRelevantAnimationPlayer();
            if (potentialPlayer.isPresent()) {
                AnimationPlayer player = potentialPlayer.get();
                float transitionTimeTicks = transitionContext.transitionDuration().inTicks() * crossFadeWeight;
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
        };
    }

    /**
     * Creates a new state transition builder with the provided state identifier as the target.
     *
     * @param target Destination state identifier of the transition
     */
    public static <S extends Enum<S>> Builder<S> builder(S target) {
        return new Builder<>(target);
    }

    @Override
    public int compareTo(@NotNull StateTransition other) {
        return Integer.compare(other.priority(), this.priority());
    }

    public static class Builder<S extends Enum<S>> {
        private final S target;
        private Predicate<TransitionContext> conditionPredicate;
        private Transition transition;
        private int priority;
        private Consumer<PoseFunction.FunctionEvaluationState> onTransitionTakenListener;
        private boolean canInterruptOtherTransitions;
        private boolean automaticTransition;
        private float automaticTransitionCrossfadeWeight;

        private Builder(S target) {
            this.conditionPredicate = null;
            this.target = target;
            this.transition = Transition.SINGLE_TICK;
            this.priority = 50;
            this.onTransitionTakenListener = evaluationState -> {};
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
        public Builder<S> setCanInterruptOtherTransitions(boolean canInterruptOtherTransitions) {
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
        public Builder<S> isTakenOnAnimationFinished(float crossFadeWeight) {
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
        public final Builder<S> isTakenIfTrue(Predicate<TransitionContext> conditionPredicate) {
            this.conditionPredicate = conditionPredicate;
            return this;
        }

        /**
         * Sets the transition timing properties for the state transition. Default is a single-tick linear transition.
         *
         * @param transition The {@link Transition} to use
         */
        public Builder<S> setTiming(Transition transition) {
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
        public Builder<S> setPriority(int priority) {
            this.priority = priority;
            return this;
        }

        /**
         * Binds an event to be called every time this transition is entered in the state machine.
         *
         * <p>Multiple events can be chained together with multiple calls to this method.</p>
         */
        public Builder<S> bindToOnTransitionTaken(Consumer<PoseFunction.FunctionEvaluationState> onTransitionTaken) {
            this.onTransitionTakenListener = this.onTransitionTakenListener.andThen(onTransitionTaken);
            return this;
        }

        public StateTransition<S> build() {
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
                this.conditionPredicate = this.conditionPredicate.or(makeMostRelevantAnimationPlayerFinishedCondition(this.automaticTransitionCrossfadeWeight));
            }
            return new StateTransition<>(this.target, this.conditionPredicate, this.transition, this.priority, this.onTransitionTakenListener, this.automaticTransition);
        }
    }

    public record TransitionContext(
            OnTickDriverContainer driverContainer,
            TimeSpan timeElapsedInCurrentState,
            float currentStateWeight,
            float previousStateWeight,
            PoseFunction<LocalSpacePose> currentStateInput,
            TimeSpan transitionDuration
    ) {
        public static TransitionContext of(OnTickDriverContainer dataContainer, TimeSpan timeElapsedInCurrentState, float currentStateWeight, float previousStateWeight, PoseFunction<LocalSpacePose> currentStateInput, TimeSpan transitionDuration) {
            return new TransitionContext(dataContainer, timeElapsedInCurrentState, currentStateWeight, previousStateWeight, currentStateInput, transitionDuration);
        }

        public Optional<AnimationPlayer> findMostRelevantAnimationPlayer() {
            Optional<PoseFunction<?>> foundPoseFunction = this.currentStateInput.searchDownChainForMostRelevant(poseFunction -> poseFunction instanceof AnimationPlayer);
            return foundPoseFunction.map(poseFunction -> (AnimationPlayer) poseFunction);
        }
    }
}
