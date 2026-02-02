package com.trainguy9512.locomotion.animation.pose.function.statemachine;

import com.google.common.collect.Maps;
import com.trainguy9512.locomotion.animation.data.DriverGetter;
import com.trainguy9512.locomotion.animation.data.PoseTickEvaluationContext;
import com.trainguy9512.locomotion.animation.driver.DriverKey;
import com.trainguy9512.locomotion.animation.driver.VariableDriver;
import com.trainguy9512.locomotion.animation.pose.LocalSpacePose;
import com.trainguy9512.locomotion.animation.data.PoseCalculationContext;
import com.trainguy9512.locomotion.animation.pose.function.PoseFunction;
import com.trainguy9512.locomotion.animation.pose.function.TimeBasedPoseFunction;
import com.trainguy9512.locomotion.animation.util.TimeSpan;
import com.trainguy9512.locomotion.animation.util.Transition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Pose function that manages transitions between a set of enum-identified states based on instance-defined transition logic.
 *
 * <p>Each state has its own pose function and a set of potential paths to other states that are taken
 * based on a condition predicate</p>
 *
 * <p>State machine functions are useful for animation functionality where specific animations
 * should be triggered in sequence based on an entity's actions, such as a jumping animation triggering when the entity
 * is both off the ground and moving upwards, which would transition into a falling animation and then
 * back to a standing animation once no longer falling.</p>
 */
public class StateMachineFunction extends TimeBasedPoseFunction<LocalSpacePose> {

    private static final Logger LOGGER = LogManager.getLogger("Locomotion/StateMachineFunction");

    private final Map<String, StateDefinition> states;
    private final Function<DriverGetter, String> initialStateFunction;
    private final List<StateBlendLayer> stateBlendLayerStack;

    private long lastUpdateTick;
    private final boolean resetsUponRelevant;
    private final List<DriverKey<VariableDriver<String>>> driversToUpdateOnStateChanged;

    private StateMachineFunction(
            Map<String, StateDefinition> states,
            Function<DriverGetter, String> initialStateFunction,
            boolean resetsUponRelevant,
            List<DriverKey<VariableDriver<String>>> driversToUpdateOnStateChanged
    ) {
        super(context -> true, context -> 1f, TimeSpan.ZERO);
        this.states = states;
        this.initialStateFunction = initialStateFunction;
        this.stateBlendLayerStack = new ArrayList<>();

        this.lastUpdateTick = 0;
        this.resetsUponRelevant = resetsUponRelevant;
        this.driversToUpdateOnStateChanged = driversToUpdateOnStateChanged;
    }

    @Override
    public @NotNull LocalSpacePose compute(PoseCalculationContext context) {
        // If the list of active states is empty, throw an error because this should never be the case unless something has gone wrong.
        if(this.stateBlendLayerStack.isEmpty()){
            LOGGER.error("State machine active states list found to be empty. Throwing error...");
            LOGGER.error("States in state machine: {}", this.states.keySet());
            throw new IllegalStateException("State machine found to have no active states");
        }
        // Add all calculated poses to a map, because there can be multiple instances of the same
        // state in the stack but each state should only have its pose calculated once.
        Set<String> uniqueStatesInLayerStack = this.getStatesInLayerStack();
        Map<String, LocalSpacePose> layerStackPoses = Maps.newHashMapWithExpectedSize(uniqueStatesInLayerStack.size());
        for (String stateIdentifier : uniqueStatesInLayerStack) {
            layerStackPoses.put(stateIdentifier, this.states.get(stateIdentifier).inputFunction.compute(context));
        }

        // Blend the poses from the layer stack poses map, starting with the first pose.
        LocalSpacePose pose = layerStackPoses.get(this.stateBlendLayerStack.getFirst().identifier);

        if (this.stateBlendLayerStack.size() > 1) {
            for (StateBlendLayer stateBlendLayer : this.stateBlendLayerStack.subList(1, stateBlendLayerStack.size())) {
                pose.interpolatedByTransition(
                        layerStackPoses.get(stateBlendLayer.identifier),
                        stateBlendLayer.weight.getInterpolatedValue(context.partialTicks()),
                        stateBlendLayer.entranceTransition.transition(),
                        null
                );
            }
        }
        return pose;
    }

    @Override
    public void tick(PoseTickEvaluationContext context) {
        // Add to the current elapsed ticks
        super.tick(context);

        this.takeInitialStateIfResetting(context);

        Optional<StateTransition> potentialStateTransition = this.testForOutboundTransition(context);

        // If there is a transition occurring, add a new state blend layer instance to the layer stack, and resets the elapsed time in the state machine.
        String stateBeingEntered = null;
        if (potentialStateTransition.isPresent()) {
            StateTransition transition = potentialStateTransition.get();
            this.takeTransition(context, transition);
            stateBeingEntered = transition.target();
        }

        // Tick each layer on the blend layer instance stack.
        this.stateBlendLayerStack.forEach(StateBlendLayer::tick);
        this.popOverriddenStates();

        this.tickPoseFunctionsInActiveStates(context, stateBeingEntered);
    }

    private void takeInitialStateIfResetting(PoseTickEvaluationContext context) {
        // If the state machine has no active states, initialize it using the initial state function.
        // If the state machine is just now becoming relevant again after not being relevant, re-initialize it.
        boolean layerStackIsEmpty = this.stateBlendLayerStack.isEmpty();
        boolean hasBecomeRelevant = context.currentTick() - 1 > this.lastUpdateTick;
        if (layerStackIsEmpty || (hasBecomeRelevant && this.resetsUponRelevant)) {
            this.stateBlendLayerStack.clear();
            String initialStateIdentifier = this.initialStateFunction.apply(context);
            if (this.states.containsKey(initialStateIdentifier)) {
                this.stateBlendLayerStack.addLast(new StateBlendLayer(
                        initialStateIdentifier,
                        StateTransition.builder(initialStateIdentifier)
                                .setTiming(Transition.INSTANT)
                                .isTakenIfTrue(transitionContext -> true)
                                .build())
                );
            } else {
                throw new IllegalStateException("Initial state " + initialStateIdentifier + " not found to be present in the state machine");
            }
        }
        this.lastUpdateTick = context.currentTick();
    }

    private Optional<StateTransition> testForOutboundTransition(PoseTickEvaluationContext context) {
        // Get the current active state
        String currentActiveStateIdentifier = this.stateBlendLayerStack.getLast().identifier;
        StateDefinition currentActiveStateDefinition = this.states.get(currentActiveStateIdentifier);

        // Filter each potential state transition by whether it's valid, then filter by whether its condition predicate is true,
        // then shuffle it in order to make equal priority transitions randomized and re-order the valid transitions by filter order.
        return currentActiveStateDefinition.outboundTransitions.stream()
                .filter(stateTransition -> {
                    boolean transitionTargetIncludedInThisMachine = this.states.containsKey(stateTransition.target());
                    boolean targetIsNotCurrentActiveState = !Objects.equals(stateTransition.target(), currentActiveStateIdentifier);
                    if(transitionTargetIncludedInThisMachine && targetIsNotCurrentActiveState){
                        StateTransitionContext transitionContext = new StateTransitionContext(
                                context,
                                TimeSpan.ofTicks(this.ticksElapsed.getCurrentValue()),
                                this.stateBlendLayerStack.getLast().weight.getCurrentValue(),
                                this.stateBlendLayerStack.getLast().weight.getPreviousValue(),
                                this.states.get(currentActiveStateIdentifier).inputFunction,
                                stateTransition.transition().duration()
                        );
                        return stateTransition.conditionPredicate().test(transitionContext);
                    }
                    return false;
                })
                .collect(Collectors.collectingAndThen(Collectors.toList(), collected -> {
                    Collections.shuffle(collected);
                    return collected;
                }))
                .stream()
                .sorted()
                .findFirst();
    }

    private void takeTransition(PoseTickEvaluationContext context, StateTransition transition) {
        transition.onTransitionTakenListener().accept(context);
        this.driversToUpdateOnStateChanged.forEach(driverKey -> {
            LOGGER.info(driverKey.getIdentifier());
            context.getDriver(driverKey).setValue(transition.target());
        });
        this.stateBlendLayerStack.addLast(new StateBlendLayer(transition.target(), transition));
        this.resetTime();
    }

    private void popOverriddenStates() {
        // Iterate through the layer stack top to bottom.
        // If a layer is found to be fully active, meaning it's overriding all states beneath it, remove all states beneath it.
        boolean higherStateIsFullyOverriding = false;
        List<StateBlendLayer> inactiveLayers = new ArrayList<>();
        for (StateBlendLayer stateBlendLayer : this.stateBlendLayerStack.reversed()) {
            if (higherStateIsFullyOverriding) {
                inactiveLayers.add(stateBlendLayer);
            } else if (stateBlendLayer.isIsFullyActive) {
                higherStateIsFullyOverriding = true;
            }
        }
        this.stateBlendLayerStack.removeAll(inactiveLayers);
    }

    private void tickPoseFunctionsInActiveStates(PoseTickEvaluationContext context, @Nullable String stateBeingEntered) {
        for (String stateIdentifier : this.getStatesInLayerStack()) {
            StateDefinition stateDefinition = this.states.get(stateIdentifier);
            PoseFunction<?> statePoseFunction = stateDefinition.inputFunction;
            boolean shouldResetStatePoseFunction = Objects.equals(stateBeingEntered, stateIdentifier) && stateDefinition.resetUponEntry;
            statePoseFunction.tick(shouldResetStatePoseFunction ? context.markedForReset() : context);
        }
    }

    private Set<String> getStatesInLayerStack() {
        Set<String> set = new HashSet<>();
        this.stateBlendLayerStack.forEach(stateBlendLayer -> set.add(stateBlendLayer.identifier));
        return set;
    }

    @Override
    public PoseFunction<LocalSpacePose> wrapUnique() {
        Builder builder = StateMachineFunction.builder(this.initialStateFunction);
        builder.resetsUponRelevant(this.resetsUponRelevant);
        this.driversToUpdateOnStateChanged.forEach(builder::bindDriverToCurrentActiveState);
        this.states.forEach((identifier, state) ->
                builder.defineState(
                        StateDefinition.builder(state).wrapUniquePoseFunction().build()
                )
        );
        return builder.build();
    }

    @Override
    public Optional<PoseFunction<?>> searchDownChainForMostRelevant(Predicate<PoseFunction<?>> findCondition) {
        // Test this pose function first
        if (findCondition.test(this)) {
            return Optional.of(this);
        }
        // Search for an animation player in the state blend layer stack from most active to least active.
        for (StateBlendLayer stateBlendLayer : this.stateBlendLayerStack.reversed()) {
            var potentialPlayer = this.states.get(stateBlendLayer.identifier).inputFunction.searchDownChainForMostRelevant(findCondition);
            if (potentialPlayer.isPresent()) {
                return potentialPlayer;
            }
        }
        return Optional.empty();
    }

    private static class StateBlendLayer {
        private final String identifier;
        private final StateTransition entranceTransition;
        private final VariableDriver<Float> weight;
        private final float weightIncrement;
        private boolean isIsFullyActive;

        private StateBlendLayer(String identifier, StateTransition entranceTransition) {
            this.identifier = identifier;
            this.entranceTransition = entranceTransition;
            this.weight = VariableDriver.ofFloat(() -> 0f);
            this.weightIncrement = 1 / Math.max(this.entranceTransition.transition().duration().inTicks(), 0.01f);
            this.isIsFullyActive = false;
        }

        private void tick() {
            this.weight.pushCurrentToPrevious();
            this.weight.modifyValue(currentValue -> Math.min(1, currentValue + weightIncrement));
            if (this.weight.getCurrentValue() == 1 && this.weight.getPreviousValue() == 1) {
                this.isIsFullyActive = true;
            }
        }

        @Override
        public String toString() {
            return "StateBlendLayer{" + identifier + " " + weight.getCurrentValue() + "}";
        }
    }

    /**
     * Creates a new state machine builder.
     *
     * <p>Every time the state machine is initialized, the provided function is
     * ran to determine the entry state.</p>
     *
     * @param entryStateFunction        Function to determine the entry state
     */
    public static Builder builder(Function<DriverGetter, String> entryStateFunction) {
        return new Builder(entryStateFunction);
    }

    public static class Builder {

        private final Function<DriverGetter, String> initialState;
        private final Map<String, StateDefinition> states;
        private final List<StateAlias> stateAliases;

        private boolean resetUponRelevant;
        private final List<DriverKey<VariableDriver<String>>> driversToUpdateOnStateChanged;

        protected Builder(Function<DriverGetter, String> initialState) {
            this.initialState = initialState;
            this.states = Maps.newHashMap();
            this.stateAliases = new ArrayList<>();

            this.resetUponRelevant = false;
            this.driversToUpdateOnStateChanged = new ArrayList<>();
        }

        /**
         * Adds a state to the state machine builder.
         * @param stateDefinition                 State created with a {@link StateDefinition.Builder}
         */
        public Builder defineState(StateDefinition stateDefinition) {
            if (this.states.containsKey(stateDefinition.identifier)) {
                throw new IllegalStateException("Cannot add state " + stateDefinition.identifier + " twice to the same state machine.");
            } else {
                this.states.put(stateDefinition.identifier, stateDefinition);
            }
            return this;
        }

        /**
         * Sets this state machine to reset to the initial state every time the state machine goes from being irrelevant to relevant.
         */
        public Builder resetsUponRelevant(boolean resetUponRelevant) {
            this.resetUponRelevant = resetUponRelevant;
            return this;
        }

        /**
         * Adds a state alias to the state machine builder.
         *
         * <p>A state alias is a shortcut function that allows you to add transitions from
         * multiple states at once, as a many-to-one transition.</p>
         *
         * <p>One example of how this could be used would be for a jumping animation state that
         * can be transitioned from the walking, idle, or crouching states with the same transition
         * properties and condition. Rather than individually adding the transition to each state,
         * this could be done through a state alias.</p>
         *
         * @param stateAlias            State alias created with a {@link StateAlias.Builder}
         */
        public Builder addStateAlias(StateAlias stateAlias) {
            this.stateAliases.add(stateAlias);
            return this;
        }

        public Builder bindDriverToCurrentActiveState(DriverKey<VariableDriver<String>> driverKey) {
            this.driversToUpdateOnStateChanged.add(driverKey);
            return this;
        }

        public PoseFunction<LocalSpacePose> build(){
            // Apply the state alias's outbound transitions to each of its origin states.
            for (StateAlias stateAlias : this.stateAliases) {
                for (String originState : stateAlias.originStates()) {
                    if (this.states.containsKey(originState)) {
                        StateDefinition.Builder stateBuilder = StateDefinition.builder(this.states.get(originState));
                        this.states.put(originState, stateBuilder.addOutboundTransitions(stateAlias.outboundTransitions()).build());
                    } else {
                        LOGGER.error("Failed to apply state alias for state {}, as it hasn't been added to the state machine builder.", originState);
                        throw new IllegalArgumentException("State machine transition validation failed, state alias contains states not found in state machine.");
                    }
                }
            }
            // Check that every state's outbound transitions have valid identifiers.
            for (StateDefinition stateDefinition : this.states.values()) {
                for (StateTransition transition : stateDefinition.outboundTransitions) {
                    if (!this.states.containsKey(transition.target())) {
                        LOGGER.error("State transition from states {} to {} not valid because state {} is not present in the state machine.", stateDefinition.identifier, transition.target(), transition.target());
                        throw new IllegalArgumentException("State machine transition validation failed, transition target not found in state machine.");
                    }
                }
            }
            // Check that each state has at least one outbound transitions.
            for (StateDefinition stateDefinition : this.states.values()) {
                if (stateDefinition.outboundTransitions.isEmpty()) {
                    LOGGER.warn("State {} in state machine contains no outbound transitions. If this state is entered, it will have no valid path out without re-initializing the state!", stateDefinition.identifier);
                }
            }
            return new StateMachineFunction(this.states, this.initialState, this.resetUponRelevant, this.driversToUpdateOnStateChanged);
        }
    }

}
