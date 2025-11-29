package com.trainguy9512.locomotion.animation.pose.function.statemachine;

import com.trainguy9512.locomotion.LocomotionMain;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public record StateAlias(Set<String> originStates, List<StateTransition> outboundTransitions) {

    private static final Logger LOGGER = LogManager.getLogger("Locomotion/StateAlias");

    /**
     * Creates a new state alias builder.
     *
     * @param originStates      States that the alias' transitions can originate from.
     */
    public static Builder builder(Set<String> originStates) {
        return new Builder(originStates);
    }

    public static class Builder {

        private final Set<String> originStates;
        private final List<StateTransition> outboundTransitions;

        private Builder(Set<String> originStates) {
            this.originStates = new HashSet<>(originStates);
            this.outboundTransitions = new ArrayList<>();
        }

        /**
         * Adds a set of states that the alias' transitions can originate from.
         * @param states        State identifiers
         */
        public final Builder addOriginatingStates(Set<String> states) {
            this.originStates.addAll(states);
            return this;
        }

        /**
         * Adds a state that the alias' transitions can originate from.
         * @param state         State identifier
         */
        public final Builder addOriginatingState(String state) {
            this.originStates.add(state);
            return this;
        }

        /**
         * Assigns a potential outbound transitions to this state alias.
         *
         * @param transition    Outbound transitions.
         */
        public Builder addOutboundTransition(StateTransition transition) {
            if (this.originStates.contains(transition.target())) {
                LOGGER.warn("Cannot add state transition to state {} from state alias that contains it already: {}", transition.target(), this.originStates);
            }
            this.outboundTransitions.add(transition);
            return this;
        }

        public StateAlias build() {
            return new StateAlias(this.originStates, this.outboundTransitions);
        }
    }
}
