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

    public static boolean isJumpingAndCanBypassJumpAnimation(StateTransition.TransitionContext context) {
        return isJumping(context) && context.timeElapsedInCurrentState().in60FramesPerSecond() < 12;
    }

    public static boolean isJumping(StateTransition.TransitionContext context) {
        boolean isJumping = context.driverContainer().getDriverValue(FirstPersonDrivers.IS_JUMPING);
        boolean isGrounded = context.driverContainer().getDriverValue(FirstPersonDrivers.IS_GROUNDED);
        return isJumping && !isGrounded;
    }

    public static boolean isWalking(StateTransition.TransitionContext context) {
        return context.driverContainer().getDriverValue(FirstPersonDrivers.IS_MOVING);
    }

    public static boolean isNotWalking(StateTransition.TransitionContext context) {
        return !isWalking(context);
    }

    public static boolean isCancellingWalk(StateTransition.TransitionContext context) {
        return isNotWalking(context) && !StateTransition.CURRENT_TRANSITION_FINISHED.test(context);
    }

    public static boolean isEasingOutOfWalk(StateTransition.TransitionContext context) {
        return isNotWalking(context) && StateTransition.CURRENT_TRANSITION_FINISHED.test(context);
    }

    public static boolean isCrouching(StateTransition.TransitionContext context) {
        return context.driverContainer().getDriverValue(FirstPersonDrivers.IS_CROUCHING);
    }

    public static boolean isNotCrouching(StateTransition.TransitionContext context) {
        return !isCrouching(context);
    }

    /**
     * Returns a pose function of the input but with the crouch pose offset added on top.
     */
    public static PoseFunction<LocalSpacePose> getCrouchedPose(PoseFunction<LocalSpacePose> inputPose) {
        PoseFunction<LocalSpacePose> crouchPose = SequenceEvaluatorFunction.builder(FirstPersonAnimationSequences.GROUND_MOVEMENT_CROUCH_OUT).build();
        PoseFunction<LocalSpacePose> basePose = SequenceEvaluatorFunction.builder(FirstPersonAnimationSequences.GROUND_MOVEMENT_POSE).build();
        PoseFunction<LocalSpacePose> makeDynamicAdditivePose = MakeDynamicAdditiveFunction.of(crouchPose, basePose);
        return ApplyAdditiveFunction.of(inputPose, makeDynamicAdditivePose);
    }

    private static State<GroundMovementStates> buildIdleState(
            GroundMovementStates idleState,
            GroundMovementStates walkingState,
            PoseFunction<LocalSpacePose> poseFunction
    ) {
        return State.builder(idleState, poseFunction)
                // Begin walking if the player is moving horizontally
                .addOutboundTransition(StateTransition.builder(walkingState)
                        .isTakenIfTrue(FirstPersonAdditiveMovement::isWalking)
                        .setTiming(Transition.builder(TimeSpan.ofSeconds(0.2f)).setEasement(Easing.SINE_OUT).build())
                        .build())
                .build();
    }

    private static State<GroundMovementStates> buildWalkingState(
            GroundMovementStates walkingState,
            GroundMovementStates stoppingState,
            GroundMovementStates idleState,
            PoseFunction<LocalSpacePose> poseFunction
    ) {
        return State.builder(walkingState, poseFunction)
                // Stop walking with the walk-to-stop animation if the player's already been walking for a bit.
                .addOutboundTransition(StateTransition.builder(stoppingState)
                        .isTakenIfTrue(FirstPersonAdditiveMovement::isEasingOutOfWalk)
                        .setTiming(Transition.builder(TimeSpan.ofSeconds(0.2f)).setEasement(Easing.SINE_IN_OUT).build())
                        .build())
                // Stop walking directly into the idle animation if the player only just began walking.
                .addOutboundTransition(StateTransition.builder(idleState)
                        .isTakenIfTrue(FirstPersonAdditiveMovement::isCancellingWalk)
                        .setTiming(Transition.builder(TimeSpan.ofSeconds(0.3f)).setEasement(Easing.SINE_IN_OUT).build())
                        .build())
                .build();
    }

    private static State<GroundMovementStates> buildStoppingState(
            GroundMovementStates walkingState,
            GroundMovementStates stoppingState,
            GroundMovementStates idleState,
            PoseFunction<LocalSpacePose> poseFunction
    ) {
        return State.builder(stoppingState, poseFunction)
                .resetsPoseFunctionUponEntry(true)
                .addOutboundTransition(StateTransition.builder(idleState)
                        .isTakenIfMostRelevantAnimationPlayerFinishing(0f)
                        .setTiming(Transition.builder(TimeSpan.ofSeconds(1f)).setEasement(Easing.SINE_IN_OUT).build())
                        .build())
                .addOutboundTransition(StateTransition.builder(walkingState)
                        .isTakenIfTrue(StateTransition.CURRENT_TRANSITION_FINISHED.and(FirstPersonAdditiveMovement::isWalking))
                        .setTiming(Transition.builder(TimeSpan.ofSeconds(0.3f)).setEasement(Easing.SINE_IN_OUT).build())
                        .build())
                .build();
    }

    private static State<GroundMovementStates> buildJumpState(
            GroundMovementStates jumpState,
            GroundMovementStates fallingState,
            GroundMovementStates softLandState,
            PoseFunction<LocalSpacePose> poseFunction
    ) {
        return State.builder(jumpState, poseFunction)
                .resetsPoseFunctionUponEntry(true)
                // Automatically move into the falling animation player
                .addOutboundTransition(StateTransition.builder(fallingState)
                        .isTakenIfMostRelevantAnimationPlayerFinishing(1f)
                        .setTiming(Transition.builder(TimeSpan.of60FramesPerSecond(19)).setEasement(Easing.CUBIC_OUT).build())
                        .build())
                // If the player lands before it can move into the falling animation, go straight to the landing animation as long as the jump state is fully transitioned.
                .addOutboundTransition(StateTransition.builder(softLandState)
                        .isTakenIfTrue(StateTransition.CURRENT_TRANSITION_FINISHED
                                .and(StateTransition.booleanDriverPredicate(FirstPersonDrivers.IS_GROUNDED))
                        )
                        .setTiming(Transition.SINGLE_TICK)
                        .build())
                .build();
    }

    private static StateAlias<GroundMovementStates> buildGroundedToInAirStateAlias(
            Set<GroundMovementStates> originStates,
            GroundMovementStates jumpState,
            GroundMovementStates walkToFallingState
    ) {
        return StateAlias.builder(originStates)
                // Transition to the jumping animation if the player is jumping.
                .addOutboundTransition(StateTransition.builder(jumpState)
                        .isTakenIfTrue(FirstPersonAdditiveMovement::isJumping)
                        .setTiming(Transition.SINGLE_TICK)
                        .setPriority(60)
                        .build())
                // Transition to the jumping animation if the player is falling.
                .addOutboundTransition(StateTransition.builder(walkToFallingState)
                        .isTakenIfTrue(StateTransition.booleanDriverPredicate(FirstPersonDrivers.IS_GROUNDED).negate())
                        .setTiming(Transition.builder(TimeSpan.ofTicks(2)).setEasement(Easing.SINE_IN_OUT).build())
                        .setPriority(50)
                        .build())
                .build();
    }

    private static StateAlias<GroundMovementStates> buildLandStateAlias(
            Set<GroundMovementStates> landStates,
            GroundMovementStates idleState,
            GroundMovementStates walkingState,
            GroundMovementStates fallingState
    ) {
        return StateAlias.builder(landStates)
                // If the falling animation is finishing and the player is not walking, play the idle animation.
                .addOutboundTransition(StateTransition.builder(idleState)
                        .isTakenIfTrue(StateTransition.MOST_RELEVANT_ANIMATION_PLAYER_HAS_FINISHED.and(FirstPersonAdditiveMovement::isNotWalking))
                        .setTiming(Transition.builder(TimeSpan.ofSeconds(1)).setEasement(Easing.SINE_IN_OUT).build())
                        .setPriority(50)
                        .build())
                // If the falling animation is finishing and the player is walking, play the walking animation.
                .addOutboundTransition(StateTransition.builder(walkingState)
                        .isTakenIfTrue(FirstPersonAdditiveMovement::isWalking)
                        .setTiming(Transition.builder(TimeSpan.ofSeconds(0.5f)).setEasement(Easing.SINE_IN_OUT).build())
                        .setPriority(60)
                        .build())
                // Transition to the jumping animation if the player is jumping, but the landing animation has only just begun to play.
                .addOutboundTransition(StateTransition.builder(fallingState)
                        .isTakenIfTrue(FirstPersonAdditiveMovement::isJumpingAndCanBypassJumpAnimation)
                        .setTiming(Transition.builder(TimeSpan.ofSeconds(0.2f)).setEasement(Easing.SINE_OUT).build())
                        .setPriority(70)
                        .build())
                .build();
    }

    public static PoseFunction<LocalSpacePose> constructPoseFunction(CachedPoseContainer cachedPoseContainer) {

        PoseFunction<LocalSpacePose> idleAnimationPlayer = BlendPosesFunction.builder(SequenceEvaluatorFunction.builder(FirstPersonAnimationSequences.GROUND_MOVEMENT_IDLE).build())
                .addBlendInput(SequencePlayerFunction.builder(FirstPersonAnimationSequences.GROUND_MOVEMENT_IDLE).looping(true).build(), evaluationState -> 0.6f)
                .build();
        PoseFunction<LocalSpacePose> stoppingPoseFunction = SequencePlayerFunction.builder(FirstPersonAnimationSequences.GROUND_MOVEMENT_WALK_TO_STOP).setPlayRate(0.6f).build();
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

        // Crouched version of movement animations
        PoseFunction<LocalSpacePose> crouchIdlePoseFunction = getCrouchedPose(idleAnimationPlayer);
        PoseFunction<LocalSpacePose> crouchStoppingPoseFunction = getCrouchedPose(stoppingPoseFunction);
        PoseFunction<LocalSpacePose> crouchWalkingPoseFunction = getCrouchedPose(walkingPoseFunction);
        PoseFunction<LocalSpacePose> crouchLandPoseFunction = getCrouchedPose(softLandPoseFunction);
        PoseFunction<LocalSpacePose> crouchJumpPoseFunction = getCrouchedPose(jumpPoseFunction);

        // Crouch in and out animations
        PoseFunction<LocalSpacePose> crouchInPoseFunction = SequencePlayerFunction.builder(FirstPersonAnimationSequences.GROUND_MOVEMENT_CROUCH_IN).setPlayRate(0.8f).build();
        PoseFunction<LocalSpacePose> crouchOutPoseFunction = SequencePlayerFunction.builder(FirstPersonAnimationSequences.GROUND_MOVEMENT_CROUCH_OUT).setPlayRate(0.9f).build();

        // State machine
        PoseFunction<LocalSpacePose> movementStateMachine = StateMachineFunction.builder(evaluationState -> GroundMovementStates.IDLE)
                .defineState(buildIdleState(GroundMovementStates.IDLE, GroundMovementStates.WALKING, idleAnimationPlayer))
                .defineState(buildWalkingState(GroundMovementStates.WALKING, GroundMovementStates.STOPPING, GroundMovementStates.IDLE, walkingPoseFunction))
                .defineState(buildStoppingState(GroundMovementStates.WALKING, GroundMovementStates.STOPPING, GroundMovementStates.IDLE, stoppingPoseFunction))
                .defineState(buildIdleState(GroundMovementStates.CROUCH_IDLE, GroundMovementStates.CROUCH_WALKING, crouchIdlePoseFunction))
                .defineState(buildWalkingState(GroundMovementStates.CROUCH_WALKING, GroundMovementStates.CROUCH_STOPPING, GroundMovementStates.CROUCH_IDLE, crouchWalkingPoseFunction))
                .defineState(buildStoppingState(GroundMovementStates.CROUCH_WALKING, GroundMovementStates.CROUCH_STOPPING, GroundMovementStates.CROUCH_IDLE, crouchStoppingPoseFunction))
                .defineState(State.builder(GroundMovementStates.CROUCH_IN, crouchInPoseFunction)
                        .resetsPoseFunctionUponEntry(true)
                        // Start crouch walking if the crouch animation is finished and the player is walking.
                        .addOutboundTransition(StateTransition.builder(GroundMovementStates.CROUCH_WALKING)
                                .isTakenIfTrue(FirstPersonAdditiveMovement::isWalking)
                                .setTiming(Transition.builder(TimeSpan.of60FramesPerSecond(20)).setEasement(Easing.SINE_IN_OUT).build())
                                .setPriority(60)
                                .build())
                        // Start crouch idle animation if the crouch animation is finished and no other transition occurs.
                        .addOutboundTransition(StateTransition.builder(GroundMovementStates.CROUCH_IDLE)
                                .isTakenIfMostRelevantAnimationPlayerFinishing(0)
                                .setTiming(Transition.builder(TimeSpan.ofSeconds(1f)).setEasement(Easing.SINE_IN_OUT).build())
                                .setPriority(50)
                                .build())
                        // Play the crouch out animation if the player is no longer crouching.
                        .addOutboundTransition(StateTransition.builder(GroundMovementStates.CROUCH_OUT)
                                .isTakenIfTrue(FirstPersonAdditiveMovement::isNotCrouching)
                                .setPriority(70)
                                .setTiming(Transition.builder(TimeSpan.ofTicks(2)).setEasement(Easing.SINE_IN_OUT).build())
                                .build())
                        .build())
                .defineState(State.builder(GroundMovementStates.CROUCH_OUT, crouchOutPoseFunction)
                        .resetsPoseFunctionUponEntry(true)
                        // Start walking if the crouch animation is finished and the player is walking.
                        .addOutboundTransition(StateTransition.builder(GroundMovementStates.WALKING)
                                .isTakenIfTrue(FirstPersonAdditiveMovement::isWalking)
                                .setTiming(Transition.builder(TimeSpan.of60FramesPerSecond(40)).setEasement(Easing.SINE_IN_OUT).build())
                                .setPriority(60)
                                .build())
                        // Start idle animation if the crouch animation is finished and no other transition occurs.
                        .addOutboundTransition(StateTransition.builder(GroundMovementStates.IDLE)
                                .isTakenIfMostRelevantAnimationPlayerFinishing(0)
                                .setTiming(Transition.builder(TimeSpan.ofSeconds(1f)).setEasement(Easing.SINE_IN_OUT).build())
                                .setPriority(50)
                                .build())
                        // Play the crouch in animation if the player begins to crouch again.
                        .addOutboundTransition(StateTransition.builder(GroundMovementStates.CROUCH_IN)
                                .isTakenIfTrue(FirstPersonAdditiveMovement::isCrouching)
                                .setPriority(70)
                                .setTiming(Transition.builder(TimeSpan.ofTicks(2)).setEasement(Easing.SINE_IN_OUT).build())
                                .build())
                        .build())
                .defineState(buildJumpState(GroundMovementStates.JUMP, GroundMovementStates.FALLING, GroundMovementStates.SOFT_LAND, jumpPoseFunction))
                .defineState(buildJumpState(GroundMovementStates.CROUCH_JUMP, GroundMovementStates.FALLING, GroundMovementStates.CROUCH_LAND, crouchJumpPoseFunction))
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
                        // Move into the crouched landing animation if the player is no longer falling but is also crouching
                        .addOutboundTransition(StateTransition.builder(GroundMovementStates.CROUCH_LAND)
                                .isTakenIfTrue(StateTransition.booleanDriverPredicate(FirstPersonDrivers.IS_GROUNDED).and(FirstPersonAdditiveMovement::isCrouching))
                                .setTiming(Transition.SINGLE_TICK)
                                .setPriority(70)
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
                                .setPriority(80)
                                .build())
                        .build())
                .defineState(State.builder(GroundMovementStates.SOFT_LAND, softLandPoseFunction)
                        .resetsPoseFunctionUponEntry(true)
                        .build())
                .defineState(State.builder(GroundMovementStates.LAND, landPoseFunction)
                        .resetsPoseFunctionUponEntry(true)
                        .build())
                .defineState(State.builder(GroundMovementStates.CROUCH_LAND, crouchLandPoseFunction)
                        .resetsPoseFunctionUponEntry(true)
                        .build())
                // Grounded to non grounded transitions for non-crouched states.
                .addStateAlias(buildGroundedToInAirStateAlias(
                        Set.of(
                            GroundMovementStates.IDLE,
                            GroundMovementStates.WALKING,
                            GroundMovementStates.STOPPING,
                            GroundMovementStates.LAND,
                            GroundMovementStates.SOFT_LAND,
                            GroundMovementStates.CROUCH_OUT
                        ),
                        GroundMovementStates.JUMP,
                        GroundMovementStates.WALK_TO_FALLING
                ))
                // Grounded to non grounded transitions for crouched states.
                .addStateAlias(buildGroundedToInAirStateAlias(
                        Set.of(
                                GroundMovementStates.CROUCH_IDLE,
                                GroundMovementStates.CROUCH_WALKING,
                                GroundMovementStates.CROUCH_STOPPING,
                                GroundMovementStates.CROUCH_LAND,
                                GroundMovementStates.CROUCH_IN
                        ),
                        GroundMovementStates.CROUCH_JUMP,
                        GroundMovementStates.WALK_TO_FALLING
                ))
                // State alias for exiting out of the landing animation while not crouched.
                .addStateAlias(buildLandStateAlias(Set.of(GroundMovementStates.LAND, GroundMovementStates.SOFT_LAND), GroundMovementStates.IDLE, GroundMovementStates.WALKING, GroundMovementStates.FALLING))
                // State alias for exiting out of the landing animation while crouched.
                .addStateAlias(buildLandStateAlias(Set.of(GroundMovementStates.CROUCH_LAND), GroundMovementStates.CROUCH_IDLE, GroundMovementStates.CROUCH_WALKING, GroundMovementStates.FALLING))
                // State alias for going from the non-crouched animations into the crouch-in animation.
                .addStateAlias(StateAlias.builder(
                                Set.of(
                                        GroundMovementStates.IDLE,
                                        GroundMovementStates.WALKING,
                                        GroundMovementStates.STOPPING,
                                        GroundMovementStates.LAND,
                                        GroundMovementStates.SOFT_LAND
                                ))
                        .addOutboundTransition(StateTransition.builder(GroundMovementStates.CROUCH_IN)
                                .isTakenIfTrue(FirstPersonAdditiveMovement::isCrouching)
                                .setTiming(Transition.SINGLE_TICK)
                                .setPriority(80)
                                .build())
                        .build())
                // State alias for going from the crouched animations into the crouch-out animation.
                .addStateAlias(StateAlias.builder(
                                Set.of(
                                        GroundMovementStates.CROUCH_IDLE,
                                        GroundMovementStates.CROUCH_WALKING,
                                        GroundMovementStates.CROUCH_STOPPING,
                                        GroundMovementStates.CROUCH_LAND
                                ))
                        .addOutboundTransition(StateTransition.builder(GroundMovementStates.CROUCH_OUT)
                                .isTakenIfTrue(FirstPersonAdditiveMovement::isNotCrouching)
                                .setTiming(Transition.SINGLE_TICK)
                                .setPriority(80)
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
        SOFT_LAND,
        CROUCH_IDLE,
        CROUCH_WALKING,
        CROUCH_STOPPING,
        CROUCH_JUMP,
        CROUCH_LAND,
        CROUCH_IN,
        CROUCH_OUT
    }
}