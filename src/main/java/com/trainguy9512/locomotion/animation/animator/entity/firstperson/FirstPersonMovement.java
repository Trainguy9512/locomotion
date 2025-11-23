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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Pose;

import java.util.Set;

public class FirstPersonMovement {

    public static String ADDITIVE_CROUCH_POSE_CACHE = "additive_crouch_pose";
    public static String MOVEMENT_WITHOUT_OVERRIDES_CACHE = "movement_without_overrides";

    public static PoseFunction<LocalSpacePose> constructWithMovementAnimations(PoseFunction<LocalSpacePose> inputPose, CachedPoseContainer cachedPoseContainer) {
        PoseFunction<LocalSpacePose> additiveMovement = constructAdditiveMovementPoseFunction(cachedPoseContainer);
        PoseFunction<LocalSpacePose> inputWithAdditiveMovement;
        inputWithAdditiveMovement = ApplyAdditiveFunction.of(
                inputPose,
                additiveMovement
        );

        cachedPoseContainer.register(MOVEMENT_WITHOUT_OVERRIDES_CACHE, inputWithAdditiveMovement, true);
        PoseFunction<LocalSpacePose> inputWithAdditiveMovementCache;
        inputWithAdditiveMovementCache = cachedPoseContainer.getOrThrow(MOVEMENT_WITHOUT_OVERRIDES_CACHE);

        PoseFunction<LocalSpacePose> additiveWithOverridingMovement;
        additiveWithOverridingMovement = constructWithOverridingMovementAnimations(inputWithAdditiveMovementCache);
        PoseFunction<LocalSpacePose> cacheWithOverridesMasked;
        cacheWithOverridesMasked = BlendPosesFunction.builder(inputWithAdditiveMovementCache)
                .addBlendInput(
                        additiveWithOverridingMovement,
                        evaluationState -> 1f,
                        FirstPersonJointAnimator.ARMS_ONLY_MASK)
                .build();

        return cacheWithOverridesMasked;
    }

    public static boolean isJumpingAndCanBypassJumpAnimation(StateTransition.TransitionContext context) {
        return isJumping(context) && context.timeElapsedInCurrentState().in60FramesPerSecond() < 12;
    }

    public static boolean isJumping(StateTransition.TransitionContext context) {
        boolean isJumping = context.driverContainer().getDriverValue(FirstPersonDrivers.IS_JUMPING);
        boolean isGrounded = context.driverContainer().getDriverValue(FirstPersonDrivers.IS_ON_GROUND);
        boolean wasJustGrounded = context.driverContainer().getDriver(FirstPersonDrivers.IS_ON_GROUND).getPreviousValue();
        return isJumping && !isGrounded && wasJustGrounded;
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

    public static PoseFunction<LocalSpacePose> constructAdditiveMovementPoseFunction(CachedPoseContainer cachedPoseContainer) {
        cachedPoseContainer.register(ADDITIVE_CROUCH_POSE_CACHE, constructAdditiveCrouchStateMachine(), true);

        PoseFunction<LocalSpacePose> pose;
        pose = constructWalkingStateMachine();
        pose = constructWithCrouchPose(cachedPoseContainer, pose);
        pose = constructWithFallingStateMachine(cachedPoseContainer, pose);
        pose = constructWithUnderwaterStateMachine(pose);

        // Blending out the additive animation based on the map in hand.
        pose = FirstPersonMap.blendAdditiveMovementIfHoldingMap(pose);

        // Making it additive at the end
        PoseFunction<LocalSpacePose> additivePose;
        PoseFunction<LocalSpacePose> additiveReferencePose;
        additiveReferencePose = SequenceEvaluatorFunction.builder(FirstPersonAnimationSequences.GROUND_MOVEMENT_POSE).build();
        additivePose = MakeDynamicAdditiveFunction.of(
                pose,
                additiveReferencePose
        );

        return additivePose;

    }

    private static boolean isDiveSwimming(StateTransition.TransitionContext context) {
        return context.driverContainer().getDriverValue(FirstPersonDrivers.IS_SWIMMING_UNDERWATER);
    }

    private static boolean isNoLongerSwimming(StateTransition.TransitionContext context) {
        return !isDiveSwimming(context);
    }

    enum OverridingMovementStates {
        IDLE,
        SWIMMING;

        private static OverridingMovementStates entryState(PoseFunction.FunctionEvaluationState evaluationState) {
            return IDLE;
        }
    }

    public static PoseFunction<LocalSpacePose> constructWithOverridingMovementAnimations(PoseFunction<LocalSpacePose> inputPose) {
        PoseFunction<LocalSpacePose> swimmingPoseFunction = SequencePlayerFunction.builder(FirstPersonAnimationSequences.OVERRIDING_MOVEMENT_SWIMMING)
                .setPlayRate(1.2f)
                .setResetStartTimeOffset(TimeSpan.of30FramesPerSecond(39))
                .setLooping(true)
                .build();

        PoseFunction<LocalSpacePose> overridingMovementStateMachine;
        overridingMovementStateMachine = StateMachineFunction.builder(OverridingMovementStates::entryState)
                .resetsUponRelevant(true)
                .defineState(State.builder(OverridingMovementStates.IDLE, inputPose)
                        .resetsPoseFunctionUponEntry(false)
                        .addOutboundTransition(StateTransition.builder(OverridingMovementStates.SWIMMING)
                                .isTakenIfTrue(FirstPersonMovement::isDiveSwimming)
                                .setCanInterruptOtherTransitions(false)
                                .setTiming(Transition.builder(TimeSpan.ofSeconds(0.4f))
                                        .setEasement(Easing.CUBIC_IN_OUT)
                                        .build())
                                .build())
                         .build())
                .defineState(State.builder(OverridingMovementStates.SWIMMING, swimmingPoseFunction)
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(OverridingMovementStates.IDLE)
                                .isTakenIfTrue(FirstPersonMovement::isNoLongerSwimming)
                                .setCanInterruptOtherTransitions(false)
                                .setTiming(Transition.builder(TimeSpan.ofSeconds(0.6f))
                                        .setEasement(Easing.QUART_IN_OUT)
                                        .build())
                                .build())
                        .build())
                .build();

        return overridingMovementStateMachine;
    }

    enum WalkingStates {
        IDLE,
        WALKING,
        STOPPING;

        private static WalkingStates entryState(PoseFunction.FunctionEvaluationState evaluationState) {
            boolean isMoving = evaluationState.driverContainer().getDriverValue(FirstPersonDrivers.IS_MOVING);
            return isMoving ? WALKING : IDLE;
        }
    }

    public static PoseFunction<LocalSpacePose> constructWalkingStateMachine() {
        PoseFunction<LocalSpacePose> idleAnimationPlayer = BlendPosesFunction.builder(
                SequenceEvaluatorFunction.builder(FirstPersonAnimationSequences.GROUND_MOVEMENT_POSE).build())
                .addBlendInput(SequencePlayerFunction.builder(FirstPersonAnimationSequences.GROUND_MOVEMENT_IDLE)
                        .setLooping(true)
                        .build(),
                        evaluationState -> 0.8f)
                .build();
        PoseFunction<LocalSpacePose> stoppingPoseFunction = SequencePlayerFunction.builder(FirstPersonAnimationSequences.GROUND_MOVEMENT_WALK_TO_STOP).setPlayRate(0.6f).build();
        PoseFunction<LocalSpacePose> walkingPoseFunction = BlendedSequencePlayerFunction.builder(FirstPersonDrivers.MODIFIED_WALK_SPEED)
                .setResetStartTimeOffset(TimeSpan.of30FramesPerSecond(5))
                .addEntry(0f, FirstPersonAnimationSequences.GROUND_MOVEMENT_POSE, 0.5f)
                .addEntry(0.5f, FirstPersonAnimationSequences.GROUND_MOVEMENT_WALKING, 2f)
                .addEntry(0.86f, FirstPersonAnimationSequences.GROUND_MOVEMENT_WALKING, 2.25f)
                .addEntry(1f, FirstPersonAnimationSequences.GROUND_MOVEMENT_WALKING, 3.5f)
                .build();

        PoseFunction<LocalSpacePose> walkingStateMachine;
        walkingStateMachine = StateMachineFunction.builder(WalkingStates::entryState)
                .resetsUponRelevant(true)
                .defineState(State.builder(WalkingStates.IDLE, idleAnimationPlayer)
                        .resetsPoseFunctionUponEntry(true)
                        // Begin walking if the player is moving horizontally
                        .addOutboundTransition(StateTransition.builder(WalkingStates.WALKING)
                                .isTakenIfTrue(FirstPersonMovement::isWalking)
                                .setTiming(Transition.builder(TimeSpan.ofSeconds(0.3f)).setEasement(Easing.EXPONENTIAL_OUT).build())
                                .build())
                        .build())
                .defineState(State.builder(WalkingStates.WALKING, walkingPoseFunction)
                        .resetsPoseFunctionUponEntry(true)
                        // Stop walking with the walk-to-stop animation if the player's already been walking for a bit.
                        .addOutboundTransition(StateTransition.builder(WalkingStates.STOPPING)
                                .isTakenIfTrue(FirstPersonMovement::isEasingOutOfWalk)
                                .setTiming(Transition.builder(TimeSpan.ofSeconds(0.2f)).setEasement(Easing.SINE_IN_OUT).build())
                                .build())
                        // Stop walking directly into the idle animation if the player only just began walking.
                        .addOutboundTransition(StateTransition.builder(WalkingStates.IDLE)
                                .isTakenIfTrue(FirstPersonMovement::isCancellingWalk)
                                .setTiming(Transition.builder(TimeSpan.ofSeconds(0.3f)).setEasement(Easing.SINE_IN_OUT).build())
                                .build())
                        .build())
                .defineState(State.builder(WalkingStates.STOPPING, stoppingPoseFunction)
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(WalkingStates.IDLE)
                                .isTakenOnAnimationFinished(0f)
                                .setTiming(Transition.builder(TimeSpan.ofSeconds(1f)).setEasement(Easing.SINE_IN_OUT).build())
                                .build())
                        .addOutboundTransition(StateTransition.builder(WalkingStates.WALKING)
                                .isTakenIfTrue(StateTransition.CURRENT_TRANSITION_FINISHED.and(FirstPersonMovement::isWalking))
                                .setTiming(Transition.builder(TimeSpan.ofSeconds(0.3f)).setEasement(Easing.SINE_IN_OUT).build())
                                .build())
                        .build())
                .build();
        return walkingStateMachine;
    }

    enum CrouchingStates {
        STANDING,
        CROUCH_IN,
        CROUCHING,
        CROUCH_OUT;

        private static CrouchingStates entryState(PoseFunction.FunctionEvaluationState evaluationState) {
//            return STANDING;
            boolean isCrouching = evaluationState.driverContainer().getDriverValue(FirstPersonDrivers.IS_CROUCHING);
            return isCrouching ? CROUCHING : STANDING;
        }
    }

    public static PoseFunction<LocalSpacePose> constructAdditiveCrouchStateMachine() {
        PoseFunction<LocalSpacePose> standingPoseFunction = SequenceEvaluatorFunction.builder(FirstPersonAnimationSequences.GROUND_MOVEMENT_POSE).build();
        PoseFunction<LocalSpacePose> crouchPoseFunction = SequenceEvaluatorFunction.builder(FirstPersonAnimationSequences.GROUND_MOVEMENT_CROUCH_OUT)
                .evaluatesPoseAt(TimeSpan.ZERO)
                .build();
        PoseFunction<LocalSpacePose> crouchInPoseFunction = SequencePlayerFunction.builder(FirstPersonAnimationSequences.GROUND_MOVEMENT_CROUCH_IN)
                .setPlayRate(0.8f)
                .build();
        PoseFunction<LocalSpacePose> crouchOutPoseFunction = SequencePlayerFunction.builder(FirstPersonAnimationSequences.GROUND_MOVEMENT_CROUCH_OUT)
                .setPlayRate(0.9f)
                .build();

        PoseFunction<LocalSpacePose> crouchStateMachine = StateMachineFunction.builder(CrouchingStates::entryState)
                .resetsUponRelevant(true)
                .defineState(State.builder(CrouchingStates.STANDING, standingPoseFunction)
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(CrouchingStates.CROUCH_IN)
                                .isTakenIfTrue(FirstPersonMovement::isCrouching)
                                .setTiming(Transition.SINGLE_TICK)
                                .setPriority(80)
                                .build())
                        .build())
                .defineState(State.builder(CrouchingStates.CROUCH_IN, crouchInPoseFunction)
                        .resetsPoseFunctionUponEntry(true)
                        // Start crouch idle animation if the crouch animation is finished and no other transition occurs.
                        .addOutboundTransition(StateTransition.builder(CrouchingStates.CROUCH_IN)
                                .isTakenOnAnimationFinished(0)
                                .setTiming(Transition.builder(TimeSpan.ofSeconds(1f)).setEasement(Easing.SINE_IN_OUT).build())
                                .setPriority(50)
                                .build())
                        // Play the crouch out animation if the player is no longer crouching.
                        .addOutboundTransition(StateTransition.builder(CrouchingStates.CROUCH_OUT)
                                .isTakenIfTrue(FirstPersonMovement::isNotCrouching)
                                .setPriority(60)
                                .setTiming(Transition.builder(TimeSpan.ofTicks(2)).setEasement(Easing.SINE_IN_OUT).build())
                                .build())
                        .build())
                .defineState(State.builder(CrouchingStates.CROUCHING, crouchPoseFunction)
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(CrouchingStates.CROUCH_OUT)
                                .isTakenIfTrue(FirstPersonMovement::isNotCrouching)
                                .setTiming(Transition.SINGLE_TICK)
                                .setPriority(80)
                                .build())
                        .build())
                .defineState(State.builder(CrouchingStates.CROUCH_OUT, crouchOutPoseFunction)
                        .resetsPoseFunctionUponEntry(true)
                        // Start idle animation if the crouch animation is finished and no other transition occurs.
                        .addOutboundTransition(StateTransition.builder(CrouchingStates.STANDING)
                                .isTakenOnAnimationFinished(0)
                                .setTiming(Transition.builder(TimeSpan.ofSeconds(1f)).setEasement(Easing.SINE_IN_OUT).build())
                                .setPriority(50)
                                .build())
                        // Play the crouch in animation if the player begins to crouch again.
                        .addOutboundTransition(StateTransition.builder(CrouchingStates.CROUCH_IN)
                                .isTakenIfTrue(FirstPersonMovement::isCrouching)
                                .setPriority(60)
                                .setTiming(Transition.builder(TimeSpan.ofTicks(2)).setEasement(Easing.SINE_IN_OUT).build())
                                .build())
                        .build())
                .build();

        PoseFunction<LocalSpacePose> additiveCrouchPose;
        additiveCrouchPose = MakeDynamicAdditiveFunction.of(crouchStateMachine, standingPoseFunction);
        return additiveCrouchPose;
    }

    public static PoseFunction<LocalSpacePose> constructWithCrouchPose(CachedPoseContainer cachedPoseContainer, PoseFunction<LocalSpacePose> inputPose) {
        PoseFunction<LocalSpacePose> additiveCrouchPose;
        additiveCrouchPose = cachedPoseContainer.getOrThrow(ADDITIVE_CROUCH_POSE_CACHE);

        PoseFunction<LocalSpacePose> inputWithAdditiveCrouchPose;
        inputWithAdditiveCrouchPose = ApplyAdditiveFunction.of(inputPose, additiveCrouchPose);

        return inputWithAdditiveCrouchPose;
    }

    enum FallingStates {
        STANDING,
        STANDING_TO_FALLING,
        FALLING,
        JUMP,
        LAND,
        SOFT_LAND;

        private static FallingStates entryState(PoseFunction.FunctionEvaluationState evaluationState) {
            boolean isGrounded = evaluationState.driverContainer().getDriverValue(FirstPersonDrivers.IS_ON_GROUND);
            return isGrounded ? STANDING : FALLING;
        }
    }

    public static PoseFunction<LocalSpacePose> constructWithFallingStateMachine(CachedPoseContainer cachedPoseContainer, PoseFunction<LocalSpacePose> standingPose) {
        PoseFunction<LocalSpacePose> jumpPoseFunction = SequencePlayerFunction.builder(FirstPersonAnimationSequences.GROUND_MOVEMENT_JUMP).build();
        PoseFunction<LocalSpacePose> fallingPoseFunction = BlendedSequencePlayerFunction.builder(FirstPersonDrivers.VERTICAL_MOVEMENT_SPEED)
                .addEntry(0.5f, FirstPersonAnimationSequences.GROUND_MOVEMENT_FALLING_UP)
                .addEntry(-0f, FirstPersonAnimationSequences.GROUND_MOVEMENT_FALLING_IN_PLACE)
                .addEntry(-1f, FirstPersonAnimationSequences.GROUND_MOVEMENT_FALLING_DOWN)
                .build();
        PoseFunction<LocalSpacePose> landPoseFunction = SequencePlayerFunction.builder(FirstPersonAnimationSequences.GROUND_MOVEMENT_LAND).build();
        PoseFunction<LocalSpacePose> softLandPoseFunction = BlendPosesFunction.builder(SequenceEvaluatorFunction.builder(FirstPersonAnimationSequences.GROUND_MOVEMENT_POSE).build())
                .addBlendInput(SequencePlayerFunction.builder(FirstPersonAnimationSequences.GROUND_MOVEMENT_LAND).setPlayRate(1f).build(), evaluationState -> 0.5f)
                .build();
        PoseFunction<LocalSpacePose> standingToFallingPoseFunction = SequenceEvaluatorFunction.builder(FirstPersonAnimationSequences.GROUND_MOVEMENT_POSE).build();

        jumpPoseFunction = constructWithCrouchPose(cachedPoseContainer, jumpPoseFunction);
        landPoseFunction = constructWithCrouchPose(cachedPoseContainer, landPoseFunction);
        softLandPoseFunction = constructWithCrouchPose(cachedPoseContainer, softLandPoseFunction);
        standingToFallingPoseFunction = constructWithCrouchPose(cachedPoseContainer, standingToFallingPoseFunction);

        PoseFunction<LocalSpacePose> fallingStateMachine;
        fallingStateMachine = StateMachineFunction.builder(FallingStates::entryState)
                .resetsUponRelevant(true)
                .defineState(State.builder(FallingStates.STANDING, standingPose)
                        .resetsPoseFunctionUponEntry(true)
                        .build())
                .defineState(State.builder(FallingStates.STANDING_TO_FALLING, standingToFallingPoseFunction)
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(FallingStates.FALLING)
                                .isTakenIfTrue(StateTransition.ALWAYS_TRUE)
                                .setTiming(Transition.builder(TimeSpan.ofSeconds(0.2f))
                                        .setEasement(Easing.SINE_OUT)
                                        .build())
                                .build())
                        .build())
                .defineState(State.builder(FallingStates.FALLING, fallingPoseFunction)
                        .resetsPoseFunctionUponEntry(true)
                        // Move into the landing animation if the player is no longer falling
                        .addOutboundTransition(StateTransition.builder(FallingStates.LAND)
                                .isTakenIfTrue(StateTransition.takeIfBooleanDriverTrue(FirstPersonDrivers.IS_ON_GROUND))
                                .setTiming(Transition.SINGLE_TICK)
                                .setPriority(50)
                                .build())
                        // Move into the landing animation if the player is no longer falling, but only just began falling.
                        .addOutboundTransition(StateTransition.builder(FallingStates.SOFT_LAND)
                                .isTakenIfTrue(StateTransition.takeIfBooleanDriverTrue(FirstPersonDrivers.IS_ON_GROUND)
                                        .and(StateTransition.CURRENT_TRANSITION_FINISHED.negate())
                                )
                                .setTiming(Transition.SINGLE_TICK)
                                .setPriority(60)
                                .build())
                        // Move into the landing animation if the player is no longer falling, but only just began falling.
                        .addOutboundTransition(StateTransition.builder(FallingStates.STANDING)
                                .isTakenIfTrue(StateTransition.takeIfBooleanDriverTrue(FirstPersonDrivers.IS_ON_GROUND)
                                        .and(StateTransition.CURRENT_TRANSITION_FINISHED.negate())
                                        .and(StateTransition.takeIfTimeInStateLessThan(TimeSpan.ofSeconds(0.1f)))
                                )
                                .setTiming(Transition.builder(TimeSpan.ofSeconds(0.4f)).setEasement(Easing.CUBIC_OUT).build())
                                .setPriority(70)
                                .build())
                        // Transition to the jumping animation if the player is jumping and grounded.
                        .addOutboundTransition(StateTransition.builder(FallingStates.JUMP)
                                .isTakenIfTrue(FirstPersonMovement::isJumping)
                                .setTiming(Transition.SINGLE_TICK)
                                .setPriority(80)
                                .build())
                        .build())
                .defineState(State.builder(FallingStates.JUMP, jumpPoseFunction)
                        .resetsPoseFunctionUponEntry(true)
                        // Automatically move into the falling animation player
                        .addOutboundTransition(StateTransition.builder(FallingStates.FALLING)
                                .isTakenOnAnimationFinished(1f)
                                .setTiming(Transition.builder(TimeSpan.of60FramesPerSecond(19)).setEasement(Easing.CUBIC_OUT).build())
                                .build())
                        // If the player lands before it can move into the falling animation, go straight to the landing animation as long as the jump state is fully transitioned.
                        .addOutboundTransition(StateTransition.builder(FallingStates.FALLING)
                                .isTakenIfTrue(StateTransition.CURRENT_TRANSITION_FINISHED
                                        .and(StateTransition.takeIfBooleanDriverTrue(FirstPersonDrivers.IS_ON_GROUND))
                                )
                                .setTiming(Transition.SINGLE_TICK)
                                .build())
                        .build())
                .defineState(State.builder(FallingStates.LAND, landPoseFunction)
                        .resetsPoseFunctionUponEntry(true)
                        .build())
                .defineState(State.builder(FallingStates.SOFT_LAND, softLandPoseFunction)
                        .resetsPoseFunctionUponEntry(true)
                        .build())
                .addStateAlias(StateAlias.builder(Set.of(
                        FallingStates.LAND,
                        FallingStates.SOFT_LAND
                ))
                        // If the falling animation is finishing and the player is not walking, play the idle animation.
                        .addOutboundTransition(StateTransition.builder(FallingStates.STANDING)
                                .isTakenIfTrue(StateTransition.MOST_RELEVANT_ANIMATION_PLAYER_HAS_FINISHED.and(FirstPersonMovement::isNotWalking))
                                .setTiming(Transition.builder(TimeSpan.ofSeconds(1)).setEasement(Easing.SINE_IN_OUT).build())
                                .setPriority(50)
                                .build())
                        // If the falling animation is finishing and the player is walking, play the walking animation.
                        .addOutboundTransition(StateTransition.builder(FallingStates.STANDING)
                                .isTakenIfTrue(FirstPersonMovement::isWalking)
                                .setTiming(Transition.builder(TimeSpan.ofSeconds(0.5f)).setEasement(Easing.SINE_IN_OUT).build())
                                .setPriority(60)
                                .build())
                        // Transition to the jumping animation if the player is jumping, but the landing animation has only just begun to play.
                        .addOutboundTransition(StateTransition.builder(FallingStates.FALLING)
                                .isTakenIfTrue(FirstPersonMovement::isJumpingAndCanBypassJumpAnimation)
                                .setTiming(Transition.builder(TimeSpan.ofSeconds(0.2f)).setEasement(Easing.SINE_OUT).build())
                                .setPriority(70)
                                .build())
                        .build())
                .addStateAlias(StateAlias.builder(Set.of(
                                FallingStates.LAND,
                                FallingStates.SOFT_LAND,
                                FallingStates.STANDING
                        ))
                        // Transition to the jumping animation if the player is jumping.
                        .addOutboundTransition(StateTransition.builder(FallingStates.JUMP)
                                .isTakenIfTrue(FirstPersonMovement::isJumping)
                                .setTiming(Transition.SINGLE_TICK)
                                .setPriority(60)
                                .build())
                        // Transition to the falling animation if the player is falling.
                        .addOutboundTransition(StateTransition.builder(FallingStates.STANDING_TO_FALLING)
                                .isTakenIfTrue(StateTransition.takeIfBooleanDriverTrue(FirstPersonDrivers.IS_ON_GROUND).negate())
                                .setTiming(Transition.builder(TimeSpan.ofTicks(2))
                                        .setEasement(Easing.SINE_IN_OUT)
                                        .build())
                                .setPriority(50)
                                .build())
                        .build())
                .build();

        return fallingStateMachine;
    }

    enum UnderwaterStates {
        IDLE,
        TREADING_UNDERWATER,
        LAND_UNDERWATER,
        ON_GROUND_UNDERWATER;

        private static UnderwaterStates entryState(PoseFunction.FunctionEvaluationState evaluationState) {
            boolean onGround = evaluationState.driverContainer().getDriverValue(FirstPersonDrivers.IS_ON_GROUND);
            return onGround ? ON_GROUND_UNDERWATER : IDLE;
        }
    }

    private static boolean isUnderWater(StateTransition.TransitionContext context) {
        return context.driverContainer().getDriverValue(FirstPersonDrivers.IS_UNDERWATER);
    }

    private static boolean isNoLongerUnderWater(StateTransition.TransitionContext context) {
        return !isUnderWater(context);
    }

    private static boolean isOnGround(StateTransition.TransitionContext context) {
        boolean isGrounded = context.driverContainer().getDriverValue(FirstPersonDrivers.IS_ON_GROUND);
        boolean isDiveSwimming = context.driverContainer().getDriverValue(FirstPersonDrivers.IS_SWIMMING_UNDERWATER);
        return isGrounded && !isDiveSwimming;
    }

    private static boolean isNoLongerOnGround(StateTransition.TransitionContext context) {
        return !isOnGround(context);
    }

    public static PoseFunction<LocalSpacePose> constructWithUnderwaterStateMachine(PoseFunction<LocalSpacePose> inputPose) {
        ResourceLocation groundMovementSwimmingSequence = FirstPersonAnimationSequences.GROUND_MOVEMENT_SWIMMING_IDLE;
        PoseFunction<LocalSpacePose> treadingUnderwaterPoseFunction = BlendedSequencePlayerFunction.builder(FirstPersonDrivers.VERTICAL_MOVEMENT_SPEED)
                .setResetStartTimeOffset(TimeSpan.of30FramesPerSecond(5))
                .addEntry(-0.05f, groundMovementSwimmingSequence, 0.5f)
                .addEntry(0f, groundMovementSwimmingSequence, 1f)
                .addEntry(0.1f, groundMovementSwimmingSequence, 1.5f)
                .addEntry(0.3f, groundMovementSwimmingSequence, 2f)
                .build();
        PoseFunction<LocalSpacePose> landUnderwaterPoseFunction = BlendPosesFunction.builder(SequenceEvaluatorFunction.builder(FirstPersonAnimationSequences.GROUND_MOVEMENT_POSE).build())
                .addBlendInput(SequencePlayerFunction.builder(FirstPersonAnimationSequences.GROUND_MOVEMENT_LAND)
                        .setPlayRate(0.7f)
                        .build(),
                        evaluationState -> 0.4f)
                .build();
        PoseFunction<LocalSpacePose> onGroundUnderwaterPoseFunction = BlendedSequencePlayerFunction.builder(FirstPersonDrivers.MODIFIED_WALK_SPEED)
                .setResetStartTimeOffset(TimeSpan.of30FramesPerSecond(5))
                .addEntry(0f, FirstPersonAnimationSequences.GROUND_MOVEMENT_POSE, 0.1f)
                .addEntry(0.3f, FirstPersonAnimationSequences.GROUND_MOVEMENT_WALKING, 0.8f)
                .build();

        PoseFunction<LocalSpacePose> underwaterStateMachine;
        underwaterStateMachine = StateMachineFunction.builder(UnderwaterStates::entryState)
                .resetsUponRelevant(true)
                .defineState(State.builder(UnderwaterStates.IDLE, inputPose)
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(UnderwaterStates.TREADING_UNDERWATER)
                                .isTakenIfTrue(FirstPersonMovement::isUnderWater)
                                .setCanInterruptOtherTransitions(false)
                                .setTiming(Transition.builder(TimeSpan.ofSeconds(0.4f))
                                        .setEasement(Easing.CUBIC_IN_OUT)
                                        .build())
                                .build())
                        .build())
                .defineState(State.builder(UnderwaterStates.TREADING_UNDERWATER, treadingUnderwaterPoseFunction)
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(UnderwaterStates.LAND_UNDERWATER)
                                .isTakenIfTrue(FirstPersonMovement::isOnGround)
                                .setCanInterruptOtherTransitions(false)
                                .setTiming(Transition.builder(TimeSpan.ofSeconds(0.2f))
                                        .setEasement(Easing.CUBIC_OUT)
                                        .build())
                                .build())
                        .build())
                .defineState(State.builder(UnderwaterStates.LAND_UNDERWATER, landUnderwaterPoseFunction)
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(UnderwaterStates.ON_GROUND_UNDERWATER)
                                .isTakenOnAnimationFinished(1f)
                                .setCanInterruptOtherTransitions(true)
                                .setTiming(Transition.builder(TimeSpan.ofSeconds(0.4f))
                                        .setEasement(Easing.CUBIC_IN_OUT)
                                        .build())
                                .build())
                        .addOutboundTransition(StateTransition.builder(UnderwaterStates.TREADING_UNDERWATER)
                                .isTakenIfTrue(FirstPersonMovement::isNoLongerOnGround)
                                .setCanInterruptOtherTransitions(false)
                                .setTiming(Transition.builder(TimeSpan.ofSeconds(0.2f))
                                        .setEasement(Easing.CUBIC_IN_OUT)
                                        .build())
                                .build())
                        .build())
                .defineState(State.builder(UnderwaterStates.ON_GROUND_UNDERWATER, onGroundUnderwaterPoseFunction)
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(UnderwaterStates.TREADING_UNDERWATER)
                                .isTakenIfTrue(FirstPersonMovement::isNoLongerOnGround)
                                .setCanInterruptOtherTransitions(false)
                                .setTiming(Transition.builder(TimeSpan.ofSeconds(0.2f))
                                        .setEasement(Easing.CUBIC_IN_OUT)
                                        .build())
                                .build())
                        .build())
                .addStateAlias(StateAlias.builder(Set.of(
                        UnderwaterStates.TREADING_UNDERWATER,
                        UnderwaterStates.ON_GROUND_UNDERWATER,
                        UnderwaterStates.LAND_UNDERWATER
                ))
                        .addOutboundTransition(StateTransition.builder(UnderwaterStates.IDLE)
                                .isTakenIfTrue(FirstPersonMovement::isNoLongerUnderWater)
                                .setCanInterruptOtherTransitions(false)
                                .setTiming(Transition.builder(TimeSpan.ofSeconds(0.6f))
                                        .setEasement(Easing.CUBIC_IN_OUT)
                                        .build())
                                .build())
                        .build())
                .build();

        return underwaterStateMachine;
    }
}