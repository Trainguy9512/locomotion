package com.trainguy9512.locomotion.animation.pose.function.statemachine;

import com.trainguy9512.locomotion.animation.pose.LocalSpacePose;
import com.trainguy9512.locomotion.animation.pose.function.PoseFunction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class StateDefinition<S extends Enum<S>> {

    private static final Logger LOGGER = LogManager.getLogger("Locomotion/State");

    protected final S identifier;
    protected final PoseFunction<LocalSpacePose> inputFunction;
    protected final List<StateTransition<S>> outboundTransitions;
    protected final boolean resetUponEntry;

    protected StateDefinition(S identifier, PoseFunction<LocalSpacePose> inputFunction, List<StateTransition<S>> outboundTransitions, boolean resetUponEntry) {
        this.identifier = identifier;
        this.inputFunction = inputFunction;
        this.outboundTransitions = outboundTransitions;
        this.resetUponEntry = resetUponEntry;

        if (!resetUponEntry) {
            for (StateTransition<S> transition : outboundTransitions) {
                if (transition.isAutomaticTransition()) {
                    LOGGER.warn("State transition to state {} in a state machine is set to be automatic based on the input sequence player, but the origin state is not set to reset upon entry. Automatic transitions are intended to be used with reset-upon-entry states, beware of unexpected behavior!", transition.target());
                }
            }
        }
    }

    /**
     * Creates a new state builder.
     *
     * @param identifier    Enum identifier that is associated with this state. Used for identifying transition targets.
     * @param inputFunction Pose function used for this state when it's active.
     */
    public static <S extends Enum<S>> Builder<S> builder(S identifier, PoseFunction<LocalSpacePose> inputFunction) {
        return new Builder<>(identifier, inputFunction);
    }

    /**
     * Creates a new state builder with the properties of the provided state.
     *
     * @param stateDefinition Identifier for the new state.
     */
    protected static <S extends Enum<S>> Builder<S> builder(StateDefinition<S> stateDefinition) {
        return new Builder<>(stateDefinition);
    }

    public static class Builder<S extends Enum<S>> {

        private final S identifier;
        private PoseFunction<LocalSpacePose> inputFunction;
        private final List<StateTransition<S>> outboundTransitions;
        private boolean resetUponEntry;

        private Builder(S identifier, PoseFunction<LocalSpacePose> inputFunction) {
            this.identifier = identifier;
            this.inputFunction = inputFunction;
            this.outboundTransitions = new ArrayList<>();
            this.resetUponEntry = false;
        }

        private Builder(StateDefinition<S> stateDefinition) {
            this.identifier = stateDefinition.identifier;
            this.inputFunction = stateDefinition.inputFunction;
            this.outboundTransitions = stateDefinition.outboundTransitions;
            this.resetUponEntry = stateDefinition.resetUponEntry;
        }

        /**
         * If true, this state will reset its pose function every time it is entered.
         */
        public Builder<S> resetsPoseFunctionUponEntry(boolean resetUponEntry) {
            this.resetUponEntry = resetUponEntry;
            return this;
        }

        /**
         * Assigns a set of potential outbound transitions to this state.
         *
         * @param transitions Set of individual transitions.
         */
        protected Builder<S> addOutboundTransitions(List<StateTransition<S>> transitions) {
            transitions.forEach(this::addOutboundTransition);
            return this;
        }

        /**
         * Assigns a potential outbound transition to this state.
         *
         * @param transition Outbound transition.
         */
        public final Builder<S> addOutboundTransition(StateTransition<S> transition) {
            this.outboundTransitions.add(transition);
            if (transition.target() == this.identifier) {
                LOGGER.warn("Cannot add state transition to state {} from the same state {}", transition.target(), this.identifier);
            }
            return this;
        }

        protected Builder<S> wrapUniquePoseFunction() {
            this.inputFunction = inputFunction.wrapUnique();
            return this;
        }

        public StateDefinition<S> build() {
            return new StateDefinition<>(this.identifier, this.inputFunction, this.outboundTransitions, this.resetUponEntry);
        }
    }
}
