package com.trainguy9512.locomotion.animation.pose.function.statemachine;

import com.trainguy9512.locomotion.animation.pose.LocalSpacePose;
import com.trainguy9512.locomotion.animation.pose.function.PoseFunction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class StateDefinition {

    private static final Logger LOGGER = LogManager.getLogger("Locomotion/State");

    protected final String identifier;
    protected final PoseFunction<LocalSpacePose> inputFunction;
    protected final List<StateTransition> outboundTransitions;
    protected final boolean resetUponEntry;

    protected StateDefinition(
            String identifier,
            PoseFunction<LocalSpacePose> inputFunction,
            List<StateTransition> outboundTransitions,
            boolean resetUponEntry
    ) {
        this.identifier = identifier;
        this.inputFunction = inputFunction;
        this.outboundTransitions = outboundTransitions;
        this.resetUponEntry = resetUponEntry;

        if (!resetUponEntry) {
            for (StateTransition transition : outboundTransitions) {
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
    public static Builder builder(String identifier, PoseFunction<LocalSpacePose> inputFunction) {
        return new Builder(identifier, inputFunction);
    }

    /**
     * Creates a new state builder with the properties of the provided state.
     *
     * @param stateDefinition Identifier for the new state.
     */
    protected static Builder builder(StateDefinition stateDefinition) {
        return new Builder(stateDefinition);
    }

    public static class Builder {

        private final String identifier;
        private PoseFunction<LocalSpacePose> inputFunction;
        private final List<StateTransition> outboundTransitions;
        private boolean resetUponEntry;

        private Builder(
                String identifier,
                PoseFunction<LocalSpacePose> inputFunction
        ) {
            this.identifier = identifier;
            this.inputFunction = inputFunction;
            this.outboundTransitions = new ArrayList<>();
            this.resetUponEntry = false;
        }

        private Builder(StateDefinition stateDefinition) {
            this.identifier = stateDefinition.identifier;
            this.inputFunction = stateDefinition.inputFunction;
            this.outboundTransitions = stateDefinition.outboundTransitions;
            this.resetUponEntry = stateDefinition.resetUponEntry;
        }

        /**
         * If true, this state will reset its pose function every time it is entered.
         */
        public Builder resetsPoseFunctionUponEntry(boolean resetUponEntry) {
            this.resetUponEntry = resetUponEntry;
            return this;
        }

        /**
         * Assigns a set of potential outbound transitions to this state.
         *
         * @param transitions Set of individual transitions.
         */
        protected Builder addOutboundTransitions(List<StateTransition> transitions) {
            transitions.forEach(this::addOutboundTransition);
            return this;
        }

        /**
         * Assigns a potential outbound transition to this state.
         *
         * @param transition Outbound transition.
         */
        public final Builder addOutboundTransition(StateTransition transition) {
            this.outboundTransitions.add(transition);
            if (Objects.equals(transition.target(), this.identifier)) {
                throw new IllegalArgumentException("Cannot add outbound transition to state " + transition.target() + " from the same state " + this.identifier);
            }
            return this;
        }

        protected Builder wrapUniquePoseFunction() {
            this.inputFunction = inputFunction.wrapUnique();
            return this;
        }

        public StateDefinition build() {
            return new StateDefinition(this.identifier, this.inputFunction, this.outboundTransitions, this.resetUponEntry);
        }
    }
}
