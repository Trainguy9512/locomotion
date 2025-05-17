package com.trainguy9512.locomotion.animation.animator.entity.firstperson;

import com.trainguy9512.locomotion.animation.pose.LocalSpacePose;
import com.trainguy9512.locomotion.animation.pose.function.*;
import com.trainguy9512.locomotion.animation.pose.function.cache.CachedPoseContainer;
import com.trainguy9512.locomotion.animation.pose.function.statemachine.State;
import com.trainguy9512.locomotion.animation.pose.function.statemachine.StateAlias;
import com.trainguy9512.locomotion.animation.pose.function.statemachine.StateMachineFunction;
import com.trainguy9512.locomotion.animation.pose.function.statemachine.StateTransition;
import com.trainguy9512.locomotion.util.Easing;
import com.trainguy9512.locomotion.util.TimeSpan;
import com.trainguy9512.locomotion.util.Transition;

import java.util.Set;
import java.util.function.Predicate;

public class FirstPersonAdditiveMovement {

    public static PoseFunction<LocalSpacePose> constructPoseFunction(CachedPoseContainer cachedPoseContainer) {

        PoseFunction<LocalSpacePose> idleAnimationPlayer = BlendPosesFunction.builder(SequenceEvaluatorFunction.builder(FirstPersonAnimationSequences.GROUND_MOVEMENT_IDLE).build())
                .addBlendInput(SequencePlayerFunction.builder(FirstPersonAnimationSequences.GROUND_MOVEMENT_IDLE).looping(true).build(), evaluationState -> 0.6f)
                .build();
        PoseFunction<LocalSpacePose> walkToStopPoseFunction = SequencePlayerFunction.builder(FirstPersonAnimationSequences.GROUND_MOVEMENT_WALK_TO_STOP).setPlayRate(0.6f).build();
        PoseFunction<LocalSpacePose> jumpPoseFunction = SequencePlayerFunction.builder(FirstPersonAnimationSequences.GROUND_MOVEMENT_JUMP).build();
        PoseFunction<LocalSpacePose> fallingPoseFunction = BlendedSequencePlayerFunction.builder(FirstPersonDrivers.VERTICAL_MOVEMENT_SPEED)
                .addEntry(0.5f, FirstPersonAnimationSequences.GROUND_MOVEMENT_FALLING_UP)
                .addEntry(-0f, FirstPersonAnimationSequences.GROUND_MOVEMENT_FALLING_IN_PLACE)
                .addEntry(-1f, FirstPersonAnimationSequences.GROUND_MOVEMENT_FALLING_DOWN)
                .build();
        PoseFunction<LocalSpacePose> walkingPoseFunction = BlendedSequencePlayerFunction.builder(FirstPersonDrivers.MODIFIED_WALK_SPEED)
                .setResetStartTimeOffset(TimeSpan.of30FramesPerSecond(5))
                .addEntry(0f, FirstPersonAnimationSequences.GROUND_MOVEMENT_POSE, 0.5f)
                .addEntry(0.5f, FirstPersonAnimationSequences.GROUND_MOVEMENT_WALKING, 2f)
                .addEntry(0.86f, FirstPersonAnimationSequences.GROUND_MOVEMENT_WALKING, 2.25f)
                .addEntry(1f, FirstPersonAnimationSequences.GROUND_MOVEMENT_WALKING, 3.5f)
                .build();

        PoseFunction<LocalSpacePose> landPoseFunction = SequencePlayerFunction.builder(FirstPersonAnimationSequences.GROUND_MOVEMENT_LAND).build();
        PoseFunction<LocalSpacePose> softLandPoseFunction = BlendPosesFunction.builder(SequenceEvaluatorFunction.builder(FirstPersonAnimationSequences.GROUND_MOVEMENT_POSE).build())
                .addBlendInput(SequencePlayerFunction.builder(FirstPersonAnimationSequences.GROUND_MOVEMENT_LAND).setPlayRate(1f).build(), evaluationState -> 0.5f)
                .build();

        Predicate<StateTransition.TransitionContext> walkingCondition = transitionContext -> transitionContext.driverContainer().getDriverValue(FirstPersonDrivers.IS_MOVING);


        PoseFunction<LocalSpacePose> movementStateMachine = StateMachineFunction.builder(evaluationState -> GroundMovementStates.IDLE)
                .defineState(State.builder(GroundMovementStates.IDLE, idleAnimationPlayer)
                        // Begin walking if the player is moving horizontally
                        .addOutboundTransition(StateTransition.builder(GroundMovementStates.WALKING)
                                .isTakenIfTrue(walkingCondition)
                                .setTiming(Transition.builder(TimeSpan.ofSeconds(0.2f)).setEasement(Easing.SINE_OUT).build())
                                .build())
                        .build())
                .defineState(State.builder(GroundMovementStates.WALKING, walkingPoseFunction)
                        // Stop walking with the walk-to-stop animation if the player's already been walking for a bit.
                        .addOutboundTransition(StateTransition.builder(GroundMovementStates.STOPPING)
                                .isTakenIfTrue(walkingCondition.negate()
                                        .and(StateTransition.CURRENT_TRANSITION_FINISHED))
                                .setTiming(Transition.builder(TimeSpan.ofSeconds(0.2f)).setEasement(Easing.SINE_IN_OUT).build())
                                .build())
                        // Stop walking directly into the idle animation if the player only just began walking.
                        .addOutboundTransition(StateTransition.builder(GroundMovementStates.IDLE)
                                .isTakenIfTrue(walkingCondition.negate()
                                        .and(StateTransition.CURRENT_TRANSITION_FINISHED.negate()))
                                .setTiming(Transition.builder(TimeSpan.ofSeconds(0.3f)).setEasement(Easing.SINE_IN_OUT).build())
                                .build())
                        .build())
                .defineState(State.builder(GroundMovementStates.STOPPING, walkToStopPoseFunction)
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(GroundMovementStates.IDLE)
                                .isTakenIfMostRelevantAnimationPlayerFinishing(0f)
                                .setTiming(Transition.builder(TimeSpan.ofSeconds(1f)).setEasement(Easing.SINE_IN_OUT).build())
                                .build())
                        .addOutboundTransition(StateTransition.builder(GroundMovementStates.WALKING)
                                .isTakenIfTrue(walkingCondition.and(StateTransition.CURRENT_TRANSITION_FINISHED))
                                .setTiming(Transition.builder(TimeSpan.ofSeconds(0.3f)).setEasement(Easing.SINE_IN_OUT).build())
                                .build())
                        .build())
                .defineState(State.builder(GroundMovementStates.JUMP, jumpPoseFunction)
                        .resetsPoseFunctionUponEntry(true)
                        // Automatically move into the falling animation player
                        .addOutboundTransition(StateTransition.builder(GroundMovementStates.FALLING)
                                .isTakenIfMostRelevantAnimationPlayerFinishing(1f)
                                .setTiming(Transition.builder(TimeSpan.of60FramesPerSecond(19)).setEasement(Easing.CUBIC_OUT).build())
                                .build())
                        // If the player lands before it can move into the falling animation, go straight to the landing animation as long as the jump state is fully transitioned.
                        .addOutboundTransition(StateTransition.builder(GroundMovementStates.LAND)
                                .isTakenIfTrue(StateTransition.CURRENT_TRANSITION_FINISHED
                                        .and(StateTransition.booleanDriverPredicate(FirstPersonDrivers.IS_GROUNDED))
                                )
                                .setTiming(Transition.SINGLE_TICK)
                                .build())
                        .build())
                .defineState(State.builder(GroundMovementStates.WALK_TO_FALLING, SequenceEvaluatorFunction.builder(FirstPersonAnimationSequences.GROUND_MOVEMENT_POSE).build())
                        .addOutboundTransition(StateTransition.builder(GroundMovementStates.FALLING)
                                .isTakenIfTrue(StateTransition.ALWAYS_TRUE)
                                .setTiming(Transition.builder(TimeSpan.ofSeconds(0.2f)).setEasement(Easing.SINE_OUT).build())
                                .build())
                        .build())
                .defineState(State.builder(GroundMovementStates.FALLING, fallingPoseFunction)
                        .resetsPoseFunctionUponEntry(true)
                        // Move into the landing animation if the player is no longer falling
                        .addOutboundTransition(StateTransition.builder(GroundMovementStates.LAND)
                                .isTakenIfTrue(
                                        StateTransition.booleanDriverPredicate(FirstPersonDrivers.IS_GROUNDED)
                                )
                                .setTiming(Transition.SINGLE_TICK)
                                .setPriority(50)
                                .build())
                        // Move into the landing animation if the player is no longer falling, but only just began falling.
                        .addOutboundTransition(StateTransition.builder(GroundMovementStates.SOFT_LAND)
                                .isTakenIfTrue(
                                        StateTransition.booleanDriverPredicate(FirstPersonDrivers.IS_GROUNDED)
                                                .and(StateTransition.CURRENT_TRANSITION_FINISHED.negate())
                                )
                                .setTiming(Transition.SINGLE_TICK)
                                .setPriority(60)
                                .build())
                        // Transition to the jumping animation if the player is jumping.
                        .addOutboundTransition(StateTransition.builder(GroundMovementStates.JUMP)
                                .isTakenIfTrue(StateTransition.booleanDriverPredicate(FirstPersonDrivers.IS_JUMPING).and(StateTransition.booleanDriverPredicate(FirstPersonDrivers.IS_GROUNDED)))
                                .setTiming(Transition.SINGLE_TICK)
                                .setPriority(70)
                                .build())
                        .build())
                .defineState(State.builder(GroundMovementStates.SOFT_LAND, softLandPoseFunction)
                        .resetsPoseFunctionUponEntry(true)
                        .build())
                .defineState(State.builder(GroundMovementStates.LAND, landPoseFunction)
                        .resetsPoseFunctionUponEntry(true)
                        .build())
                .addStateAlias(StateAlias.builder(
                                Set.of(
                                        GroundMovementStates.IDLE,
                                        GroundMovementStates.WALKING,
                                        GroundMovementStates.STOPPING,
                                        GroundMovementStates.LAND,
                                        GroundMovementStates.SOFT_LAND
                                ))
                        // Transition to the jumping animation if the player is jumping.
                        .addOutboundTransition(StateTransition.builder(GroundMovementStates.JUMP)
                                .isTakenIfTrue(StateTransition.booleanDriverPredicate(FirstPersonDrivers.IS_JUMPING)
                                        .and(StateTransition.booleanDriverPredicate(FirstPersonDrivers.IS_GROUNDED).negate()))
                                .setTiming(Transition.SINGLE_TICK)
                                .setPriority(60)
                                .build())
                        // Transition to the jumping animation if the player is falling.
                        .addOutboundTransition(StateTransition.builder(GroundMovementStates.WALK_TO_FALLING)
                                .isTakenIfTrue(StateTransition.booleanDriverPredicate(FirstPersonDrivers.IS_GROUNDED).negate())
                                .setTiming(Transition.builder(TimeSpan.ofTicks(2)).setEasement(Easing.SINE_IN_OUT).build())
                                .setPriority(50)
                                .build())
                        .build())
                .addStateAlias(StateAlias.builder(
                                Set.of(
                                        GroundMovementStates.LAND,
                                        GroundMovementStates.SOFT_LAND
                                ))
                        // If the falling animation is finishing and the player is not walking, play the idle animation.
                        .addOutboundTransition(StateTransition.builder(GroundMovementStates.IDLE)
                                .isTakenIfTrue(walkingCondition.negate().and(StateTransition.MOST_RELEVANT_ANIMATION_PLAYER_HAS_FINISHED))
                                .setTiming(Transition.builder(TimeSpan.ofSeconds(1)).setEasement(Easing.SINE_IN_OUT).build())
                                .setPriority(50)
                                .build())
                        // If the falling animation is finishing and the player is walking, play the walking animation.
                        .addOutboundTransition(StateTransition.builder(GroundMovementStates.WALKING)
                                .isTakenIfTrue(walkingCondition)
                                .setTiming(Transition.builder(TimeSpan.ofSeconds(0.5f)).setEasement(Easing.SINE_IN_OUT).build())
                                .setPriority(60)
                                .build())
                        .build())
                .build();

//        return movementStateMachine;

        return MakeDynamicAdditiveFunction.of(
                movementStateMachine,
                SequenceEvaluatorFunction.builder(FirstPersonAnimationSequences.GROUND_MOVEMENT_POSE).build()
        );
    }

    enum GroundMovementStates {
        IDLE,
        WALKING,
        STOPPING,
        JUMP,
        WALK_TO_FALLING,
        FALLING,
        LAND,
        SOFT_LAND
    }
}