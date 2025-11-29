package com.trainguy9512.locomotion.animation.animator.entity.firstperson;

import com.trainguy9512.locomotion.animation.pose.LocalSpacePose;
import com.trainguy9512.locomotion.animation.pose.function.*;
import com.trainguy9512.locomotion.animation.pose.function.cache.CachedPoseContainer;
import com.trainguy9512.locomotion.animation.pose.function.statemachine.StateDefinition;
import com.trainguy9512.locomotion.animation.pose.function.statemachine.StateAlias;
import com.trainguy9512.locomotion.animation.pose.function.statemachine.StateMachineFunction;
import com.trainguy9512.locomotion.animation.pose.function.statemachine.StateTransition;
import com.trainguy9512.locomotion.util.Easing;
import com.trainguy9512.locomotion.util.TimeSpan;
import com.trainguy9512.locomotion.util.Transition;
import net.minecraft.resources.ResourceLocation;

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
        pose = constructWithMountStateMachine(pose);

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

    public static final String OVERRIDING_MOVEMENT_IDLE_STATE = "idle";
    public static final String OVERRIDING_MOVEMENT_SWIMMING_STATE = "swimming";

    private static String getOverridingMovementEntryState(PoseFunction.FunctionEvaluationState evaluationState) {
        return OVERRIDING_MOVEMENT_IDLE_STATE;
    }

    public static PoseFunction<LocalSpacePose> constructWithOverridingMovementAnimations(PoseFunction<LocalSpacePose> inputPose) {
        PoseFunction<LocalSpacePose> swimmingPoseFunction = SequencePlayerFunction.builder(FirstPersonAnimationSequences.OVERRIDING_MOVEMENT_SWIMMING)
                .setPlayRate(1.2f)
                .setResetStartTimeOffset(TimeSpan.of30FramesPerSecond(39))
                .setLooping(true)
                .build();

        PoseFunction<LocalSpacePose> overridingMovementStateMachine;
        overridingMovementStateMachine = StateMachineFunction.builder(FirstPersonMovement::getOverridingMovementEntryState)
                .resetsUponRelevant(true)
                .defineState(StateDefinition.builder(OVERRIDING_MOVEMENT_IDLE_STATE, inputPose)
                        .resetsPoseFunctionUponEntry(false)
                        .addOutboundTransition(StateTransition.builder(OVERRIDING_MOVEMENT_SWIMMING_STATE)
                                .isTakenIfTrue(FirstPersonMovement::isDiveSwimming)
                                .setCanInterruptOtherTransitions(false)
                                .setTiming(Transition.builder(TimeSpan.ofSeconds(0.4f))
                                        .setEasement(Easing.CUBIC_IN_OUT)
                                        .build())
                                .build())
                         .build())
                .defineState(StateDefinition.builder(OVERRIDING_MOVEMENT_SWIMMING_STATE, swimmingPoseFunction)
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(OVERRIDING_MOVEMENT_IDLE_STATE)
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

    public static final String WALKING_IDLE_STATE = "idle";
    public static final String WALKING_WALKING_STATE = "walking";
    public static final String WALKING_STOPPING_STATE = "stopping";

    private static String getWalkingEntryState(PoseFunction.FunctionEvaluationState evaluationState) {
        boolean isMoving = evaluationState.driverContainer().getDriverValue(FirstPersonDrivers.IS_MOVING);
        return isMoving ? WALKING_WALKING_STATE : WALKING_IDLE_STATE;
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
        walkingStateMachine = StateMachineFunction.builder(FirstPersonMovement::getWalkingEntryState)
                .resetsUponRelevant(true)
                .defineState(StateDefinition.builder(WALKING_IDLE_STATE, idleAnimationPlayer)
                        .resetsPoseFunctionUponEntry(true)
                        // Begin walking if the player is moving horizontally
                        .addOutboundTransition(StateTransition.builder(WALKING_WALKING_STATE)
                                .isTakenIfTrue(FirstPersonMovement::isWalking)
                                .setTiming(Transition.builder(TimeSpan.ofSeconds(0.3f)).setEasement(Easing.EXPONENTIAL_OUT).build())
                                .build())
                        .build())
                .defineState(StateDefinition.builder(WALKING_WALKING_STATE, walkingPoseFunction)
                        .resetsPoseFunctionUponEntry(true)
                        // Stop walking with the walk-to-stop animation if the player's already been walking for a bit.
                        .addOutboundTransition(StateTransition.builder(WALKING_STOPPING_STATE)
                                .isTakenIfTrue(FirstPersonMovement::isEasingOutOfWalk)
                                .setTiming(Transition.builder(TimeSpan.ofSeconds(0.2f)).setEasement(Easing.SINE_IN_OUT).build())
                                .build())
                        // Stop walking directly into the idle animation if the player only just began walking.
                        .addOutboundTransition(StateTransition.builder(WALKING_IDLE_STATE)
                                .isTakenIfTrue(FirstPersonMovement::isCancellingWalk)
                                .setTiming(Transition.builder(TimeSpan.ofSeconds(0.3f)).setEasement(Easing.SINE_IN_OUT).build())
                                .build())
                        .build())
                .defineState(StateDefinition.builder(WALKING_STOPPING_STATE, stoppingPoseFunction)
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(WALKING_IDLE_STATE)
                                .isTakenOnAnimationFinished(0f)
                                .setTiming(Transition.builder(TimeSpan.ofSeconds(1f)).setEasement(Easing.SINE_IN_OUT).build())
                                .build())
                        .addOutboundTransition(StateTransition.builder(WALKING_WALKING_STATE)
                                .isTakenIfTrue(StateTransition.CURRENT_TRANSITION_FINISHED.and(FirstPersonMovement::isWalking))
                                .setTiming(Transition.builder(TimeSpan.ofSeconds(0.3f)).setEasement(Easing.SINE_IN_OUT).build())
                                .build())
                        .build())
                .build();
        return walkingStateMachine;
    }

    public static final String CROUCHING_STANDING_STATE = "standing";
    public static final String CROUCHING_CROUCH_IN_STATE = "crouch_in";
    public static final String CROUCHING_CROUCHING_STATE = "crouching";
    public static final String CROUCHING_CROUCH_OUT_STATE = "crouch_out";

    private static String getCrouchingEntryState(PoseFunction.FunctionEvaluationState evaluationState) {
        boolean isCrouching = evaluationState.driverContainer().getDriverValue(FirstPersonDrivers.IS_CROUCHING);
        return isCrouching ? CROUCHING_CROUCHING_STATE : CROUCHING_STANDING_STATE;
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

        PoseFunction<LocalSpacePose> crouchStateMachine = StateMachineFunction.builder(FirstPersonMovement::getCrouchingEntryState)
                .resetsUponRelevant(true)
                .defineState(StateDefinition.builder(CROUCHING_STANDING_STATE, standingPoseFunction)
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(CROUCHING_CROUCH_IN_STATE)
                                .isTakenIfTrue(FirstPersonMovement::isCrouching)
                                .setTiming(Transition.SINGLE_TICK)
                                .setPriority(80)
                                .build())
                        .build())
                .defineState(StateDefinition.builder(CROUCHING_CROUCH_IN_STATE, crouchInPoseFunction)
                        .resetsPoseFunctionUponEntry(true)
                        // Start crouch idle animation if the crouch animation is finished and no other transition occurs.
                        .addOutboundTransition(StateTransition.builder(CROUCHING_CROUCHING_STATE)
                                .isTakenOnAnimationFinished(0)
                                .setTiming(Transition.builder(TimeSpan.ofSeconds(1f)).setEasement(Easing.SINE_IN_OUT).build())
                                .setPriority(50)
                                .build())
                        // Play the crouch out animation if the player is no longer crouching.
                        .addOutboundTransition(StateTransition.builder(CROUCHING_CROUCH_OUT_STATE)
                                .isTakenIfTrue(FirstPersonMovement::isNotCrouching)
                                .setPriority(60)
                                .setTiming(Transition.builder(TimeSpan.ofTicks(2)).setEasement(Easing.SINE_IN_OUT).build())
                                .build())
                        .build())
                .defineState(StateDefinition.builder(CROUCHING_CROUCHING_STATE, crouchPoseFunction)
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(CROUCHING_CROUCH_OUT_STATE)
                                .isTakenIfTrue(FirstPersonMovement::isNotCrouching)
                                .setTiming(Transition.SINGLE_TICK)
                                .setPriority(80)
                                .build())
                        .build())
                .defineState(StateDefinition.builder(CROUCHING_CROUCH_OUT_STATE, crouchOutPoseFunction)
                        .resetsPoseFunctionUponEntry(true)
                        // Start idle animation if the crouch animation is finished and no other transition occurs.
                        .addOutboundTransition(StateTransition.builder(CROUCHING_STANDING_STATE)
                                .isTakenOnAnimationFinished(0)
                                .setTiming(Transition.builder(TimeSpan.ofSeconds(1f)).setEasement(Easing.SINE_IN_OUT).build())
                                .setPriority(50)
                                .build())
                        // Play the crouch in animation if the player begins to crouch again.
                        .addOutboundTransition(StateTransition.builder(CROUCHING_CROUCH_IN_STATE)
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

    public static final String FALLING_STANDING_STATE = "standing";
    public static final String FALLING_STANDING_TO_FALLING_STATE = "standing_to_falling";
    public static final String FALLING_FALLING_STATE = "falling";
    public static final String FALLING_JUMP_STATE = "jump";
    public static final String FALLING_LAND_STATE = "land";
    public static final String FALLING_SOFT_LAND_STATE = "soft_land";

    private static String getFallingEntryState(PoseFunction.FunctionEvaluationState evaluationState) {
        boolean isGrounded = evaluationState.driverContainer().getDriverValue(FirstPersonDrivers.IS_ON_GROUND);
        return isGrounded ? FALLING_STANDING_STATE : FALLING_FALLING_STATE;
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
        fallingStateMachine = StateMachineFunction.builder(FirstPersonMovement::getFallingEntryState)
                .resetsUponRelevant(true)
                .defineState(StateDefinition.builder(FALLING_STANDING_STATE, standingPose)
                        .resetsPoseFunctionUponEntry(true)
                        .build())
                .defineState(StateDefinition.builder(FALLING_STANDING_TO_FALLING_STATE, standingToFallingPoseFunction)
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(FALLING_FALLING_STATE)
                                .isTakenIfTrue(StateTransition.ALWAYS_TRUE)
                                .setTiming(Transition.builder(TimeSpan.ofSeconds(0.2f))
                                        .setEasement(Easing.SINE_OUT)
                                        .build())
                                .build())
                        .build())
                .defineState(StateDefinition.builder(FALLING_FALLING_STATE, fallingPoseFunction)
                        .resetsPoseFunctionUponEntry(true)
                        // Move into the landing animation if the player is no longer falling
                        .addOutboundTransition(StateTransition.builder(FALLING_LAND_STATE)
                                .isTakenIfTrue(StateTransition.takeIfBooleanDriverTrue(FirstPersonDrivers.IS_ON_GROUND))
                                .setTiming(Transition.SINGLE_TICK)
                                .setPriority(50)
                                .build())
                        // Move into the landing animation if the player is no longer falling, but only just began falling.
                        .addOutboundTransition(StateTransition.builder(FALLING_SOFT_LAND_STATE)
                                .isTakenIfTrue(StateTransition.takeIfBooleanDriverTrue(FirstPersonDrivers.IS_ON_GROUND)
                                        .and(StateTransition.CURRENT_TRANSITION_FINISHED.negate())
                                )
                                .setTiming(Transition.SINGLE_TICK)
                                .setPriority(60)
                                .build())
                        // Move into the landing animation if the player is no longer falling, but only just began falling.
                        .addOutboundTransition(StateTransition.builder(FALLING_STANDING_STATE)
                                .isTakenIfTrue(StateTransition.takeIfBooleanDriverTrue(FirstPersonDrivers.IS_ON_GROUND)
                                        .and(StateTransition.CURRENT_TRANSITION_FINISHED.negate())
                                        .and(StateTransition.takeIfTimeInStateLessThan(TimeSpan.ofSeconds(0.1f)))
                                )
                                .setTiming(Transition.builder(TimeSpan.ofSeconds(0.4f)).setEasement(Easing.CUBIC_OUT).build())
                                .setPriority(70)
                                .build())
                        // Transition to the jumping animation if the player is jumping and grounded.
                        .addOutboundTransition(StateTransition.builder(FALLING_JUMP_STATE)
                                .isTakenIfTrue(FirstPersonMovement::isJumping)
                                .setTiming(Transition.SINGLE_TICK)
                                .setPriority(80)
                                .build())
                        .build())
                .defineState(StateDefinition.builder(FALLING_JUMP_STATE, jumpPoseFunction)
                        .resetsPoseFunctionUponEntry(true)
                        // Automatically move into the falling animation player
                        .addOutboundTransition(StateTransition.builder(FALLING_FALLING_STATE)
                                .isTakenOnAnimationFinished(1f)
                                .setTiming(Transition.builder(TimeSpan.of60FramesPerSecond(19)).setEasement(Easing.CUBIC_OUT).build())
                                .build())
                        // If the player lands before it can move into the falling animation, go straight to the landing animation as long as the jump state is fully transitioned.
                        .addOutboundTransition(StateTransition.builder(FALLING_FALLING_STATE)
                                .isTakenIfTrue(StateTransition.CURRENT_TRANSITION_FINISHED
                                        .and(StateTransition.takeIfBooleanDriverTrue(FirstPersonDrivers.IS_ON_GROUND))
                                )
                                .setTiming(Transition.SINGLE_TICK)
                                .build())
                        .build())
                .defineState(StateDefinition.builder(FALLING_LAND_STATE, landPoseFunction)
                        .resetsPoseFunctionUponEntry(true)
                        .build())
                .defineState(StateDefinition.builder(FALLING_SOFT_LAND_STATE, softLandPoseFunction)
                        .resetsPoseFunctionUponEntry(true)
                        .build())
                .addStateAlias(StateAlias.builder(Set.of(
                                FALLING_LAND_STATE,
                                FALLING_SOFT_LAND_STATE
                ))
                        // If the falling animation is finishing and the player is not walking, play the idle animation.
                        .addOutboundTransition(StateTransition.builder(FALLING_STANDING_STATE)
                                .isTakenIfTrue(StateTransition.MOST_RELEVANT_ANIMATION_PLAYER_HAS_FINISHED.and(FirstPersonMovement::isNotWalking))
                                .setTiming(Transition.builder(TimeSpan.ofSeconds(1)).setEasement(Easing.SINE_IN_OUT).build())
                                .setPriority(50)
                                .build())
                        // If the falling animation is finishing and the player is walking, play the walking animation.
                        .addOutboundTransition(StateTransition.builder(FALLING_STANDING_STATE)
                                .isTakenIfTrue(FirstPersonMovement::isWalking)
                                .setTiming(Transition.builder(TimeSpan.ofSeconds(0.5f)).setEasement(Easing.SINE_IN_OUT).build())
                                .setPriority(60)
                                .build())
                        // Transition to the jumping animation if the player is jumping, but the landing animation has only just begun to play.
                        .addOutboundTransition(StateTransition.builder(FALLING_FALLING_STATE)
                                .isTakenIfTrue(FirstPersonMovement::isJumpingAndCanBypassJumpAnimation)
                                .setTiming(Transition.builder(TimeSpan.ofSeconds(0.2f)).setEasement(Easing.SINE_OUT).build())
                                .setPriority(70)
                                .build())
                        .build())
                .addStateAlias(StateAlias.builder(Set.of(
                                FALLING_LAND_STATE,
                                FALLING_SOFT_LAND_STATE,
                                FALLING_STANDING_STATE
                        ))
                        // Transition to the jumping animation if the player is jumping.
                        .addOutboundTransition(StateTransition.builder(FALLING_JUMP_STATE)
                                .isTakenIfTrue(FirstPersonMovement::isJumping)
                                .setTiming(Transition.SINGLE_TICK)
                                .setPriority(60)
                                .build())
                        // Transition to the falling animation if the player is falling.
                        .addOutboundTransition(StateTransition.builder(FALLING_STANDING_TO_FALLING_STATE)
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

    public static final String UNDERWATER_IDLE_STATE = "idle";
    public static final String UNDERWATER_TREADING_STATE = "treading";
    public static final String UNDERWATER_LAND_STATE = "land";
    public static final String UNDERWATER_ON_GROUND_STATE = "on_ground";

    private static String getUnderwaterEntryState(PoseFunction.FunctionEvaluationState evaluationState) {
        boolean onGround = evaluationState.driverContainer().getDriverValue(FirstPersonDrivers.IS_ON_GROUND);
        return onGround ? UNDERWATER_ON_GROUND_STATE : UNDERWATER_IDLE_STATE;
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
        underwaterStateMachine = StateMachineFunction.builder(FirstPersonMovement::getUnderwaterEntryState)
                .resetsUponRelevant(true)
                .defineState(StateDefinition.builder(UNDERWATER_IDLE_STATE, inputPose)
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(UNDERWATER_TREADING_STATE)
                                .isTakenIfTrue(FirstPersonMovement::isUnderWater)
                                .setCanInterruptOtherTransitions(false)
                                .setTiming(Transition.builder(TimeSpan.ofSeconds(0.4f))
                                        .setEasement(Easing.CUBIC_IN_OUT)
                                        .build())
                                .build())
                        .build())
                .defineState(StateDefinition.builder(UNDERWATER_TREADING_STATE, treadingUnderwaterPoseFunction)
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(UNDERWATER_LAND_STATE)
                                .isTakenIfTrue(FirstPersonMovement::isOnGround)
                                .setCanInterruptOtherTransitions(false)
                                .setTiming(Transition.builder(TimeSpan.ofSeconds(0.2f))
                                        .setEasement(Easing.CUBIC_OUT)
                                        .build())
                                .build())
                        .build())
                .defineState(StateDefinition.builder(UNDERWATER_LAND_STATE, landUnderwaterPoseFunction)
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(UNDERWATER_ON_GROUND_STATE)
                                .isTakenOnAnimationFinished(1f)
                                .setCanInterruptOtherTransitions(true)
                                .setTiming(Transition.builder(TimeSpan.ofSeconds(0.4f))
                                        .setEasement(Easing.CUBIC_IN_OUT)
                                        .build())
                                .build())
                        .addOutboundTransition(StateTransition.builder(UNDERWATER_TREADING_STATE)
                                .isTakenIfTrue(FirstPersonMovement::isNoLongerOnGround)
                                .setCanInterruptOtherTransitions(false)
                                .setTiming(Transition.builder(TimeSpan.ofSeconds(0.2f))
                                        .setEasement(Easing.CUBIC_IN_OUT)
                                        .build())
                                .build())
                        .build())
                .defineState(StateDefinition.builder(UNDERWATER_ON_GROUND_STATE, onGroundUnderwaterPoseFunction)
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(UNDERWATER_TREADING_STATE)
                                .isTakenIfTrue(FirstPersonMovement::isNoLongerOnGround)
                                .setCanInterruptOtherTransitions(false)
                                .setTiming(Transition.builder(TimeSpan.ofSeconds(0.2f))
                                        .setEasement(Easing.CUBIC_IN_OUT)
                                        .build())
                                .build())
                        .build())
                .addStateAlias(StateAlias.builder(Set.of(
                        UNDERWATER_TREADING_STATE,
                        UNDERWATER_ON_GROUND_STATE,
                        UNDERWATER_LAND_STATE
                ))
                        .addOutboundTransition(StateTransition.builder(UNDERWATER_IDLE_STATE)
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

    public static final String MOUNT_STANDING_STATE = "standing";
    public static final String MOUNT_MOUNT_ENTER_STATE = "mount_enter";
    public static final String MOUNT_MOUNTED_STATE = "mounted";

    private static String getMountEntryState(PoseFunction.FunctionEvaluationState evaluationState) {
        boolean isPassenger = evaluationState.driverContainer().getDriverValue(FirstPersonDrivers.IS_PASSENGER);
        return isPassenger ? MOUNT_MOUNTED_STATE : MOUNT_STANDING_STATE;
    }

    private static boolean isPassenger(StateTransition.TransitionContext context) {
        return context.driverContainer().getDriverValue(FirstPersonDrivers.IS_PASSENGER);
    }

    private static boolean isNotPassenger(StateTransition.TransitionContext context) {
        return !isPassenger(context);
    }

    public static PoseFunction<LocalSpacePose> constructWithMountStateMachine(PoseFunction<LocalSpacePose> inputPose) {
        PoseFunction<LocalSpacePose> mountEnterPoseFunction;
        PoseFunction<LocalSpacePose> mountedPoseFunction;
        mountEnterPoseFunction = SequencePlayerFunction.builder(FirstPersonAnimationSequences.GROUND_MOVEMENT_MOUNT_ENTER)
                .setResetStartTimeOffset(TimeSpan.of60FramesPerSecond(6))
                .build();
        mountedPoseFunction = SequenceEvaluatorFunction.builder(FirstPersonAnimationSequences.GROUND_MOVEMENT_POSE).build();

        PoseFunction<LocalSpacePose> mountStateMachine;
        mountStateMachine = StateMachineFunction.builder(FirstPersonMovement::getMountEntryState)
                .defineState(StateDefinition.builder(MOUNT_STANDING_STATE, inputPose)
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(MOUNT_MOUNT_ENTER_STATE)
                                .isTakenIfTrue(FirstPersonMovement::isPassenger)
                                .setTiming(Transition.builder(TimeSpan.of60FramesPerSecond(4f))
                                        .setEasement(Easing.SINE_IN_OUT)
                                        .build())
                                .build())
                        .build())
                .defineState(StateDefinition.builder(MOUNT_MOUNT_ENTER_STATE, mountEnterPoseFunction)
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(MOUNT_MOUNTED_STATE)
                                .isTakenOnAnimationFinished(1)
                                .setTiming(Transition.builder(TimeSpan.ofSeconds(0.2f))
                                        .setEasement(Easing.SINE_IN_OUT)
                                        .build())
                                .build())
                        .build())
                .defineState(StateDefinition.builder(MOUNT_MOUNTED_STATE, mountedPoseFunction)
                        .resetsPoseFunctionUponEntry(true)
                        .build())
                .addStateAlias(StateAlias.builder(Set.of(
                                MOUNT_MOUNTED_STATE,
                                MOUNT_MOUNT_ENTER_STATE
                        ))
                        .addOutboundTransition(StateTransition.builder(MOUNT_STANDING_STATE)
                                .isTakenIfTrue(FirstPersonMovement::isNotPassenger)
                                .setTiming(Transition.builder(TimeSpan.ofSeconds(0.4f))
                                        .setEasement(Easing.EXPONENTIAL_OUT)
                                        .build())
                                .build())
                        .build())
                .build();

        return mountStateMachine;
    }
}