package com.trainguy9512.locomotion.animation.animator.entity.firstperson;

import com.trainguy9512.locomotion.animation.joint.skeleton.BlendMask;
import com.trainguy9512.locomotion.animation.pose.LocalSpacePose;
import com.trainguy9512.locomotion.animation.pose.function.*;
import com.trainguy9512.locomotion.animation.pose.function.cache.CachedPoseContainer;
import com.trainguy9512.locomotion.animation.pose.function.montage.MontageSlotFunction;
import com.trainguy9512.locomotion.animation.pose.function.statemachine.State;
import com.trainguy9512.locomotion.animation.pose.function.statemachine.StateAlias;
import com.trainguy9512.locomotion.animation.pose.function.statemachine.StateMachineFunction;
import com.trainguy9512.locomotion.animation.pose.function.statemachine.StateTransition;
import com.trainguy9512.locomotion.util.Easing;
import com.trainguy9512.locomotion.util.TimeSpan;
import com.trainguy9512.locomotion.util.Transition;
import net.minecraft.world.InteractionHand;

import java.util.Set;
import java.util.function.Predicate;

public class FirstPersonShield {

    public static String SHIELD_MAIN_HAND_CACHE = "shield_main_hand";
    public static String SHIELD_OFF_HAND_CACHE = "shield_off_hand";

    public static String getShieldCacheIdentifier(InteractionHand hand) {
        return hand == InteractionHand.MAIN_HAND ? SHIELD_MAIN_HAND_CACHE : SHIELD_OFF_HAND_CACHE;
    }

    public static boolean isUsingShield(StateTransition.TransitionContext context, InteractionHand hand) {
        boolean isUsing = context.driverContainer().getDriverValue(FirstPersonDrivers.getUsingItemDriver(hand));
        boolean handPoseIsShield = context.driverContainer().getDriverValue(FirstPersonDrivers.getHandPoseDriver(hand)) == FirstPersonHandPose.SHIELD;
        return isUsing && handPoseIsShield;
    }

    public static boolean hasShieldEnteredCooldown(StateTransition.TransitionContext context, InteractionHand hand) {
        boolean isHandOnCooldown = context.driverContainer().getDriverValue(FirstPersonDrivers.getItemOnCooldownDriver(hand));
        boolean wasUsingShield = context.driverContainer().getDriver(FirstPersonDrivers.getUsingItemDriver(hand)).getPreviousValue();
        return isHandOnCooldown && wasUsingShield;
    }

    public static boolean isShieldNotOnCooldown(StateTransition.TransitionContext context, InteractionHand hand) {
        boolean isHandOnCooldown = context.driverContainer().getDriverValue(FirstPersonDrivers.getItemOnCooldownDriver(hand));
        return !isHandOnCooldown;
    }

    public static PoseFunction<LocalSpacePose> getShieldCachedPoseFunction(
            CachedPoseContainer cachedPoseContainer,
            InteractionHand hand
    ) {
        String shieldCacheIdentifier = getShieldCacheIdentifier(hand);
        return cachedPoseContainer.getOrThrow(shieldCacheIdentifier);
    }

    public static PoseFunction<LocalSpacePose> constructShieldPoseFunction(
            CachedPoseContainer cachedPoseContainer,
            InteractionHand hand
    ) {
        PoseFunction<LocalSpacePose> shieldStateMachine = constructShieldStateMachine(cachedPoseContainer, hand);

        String shieldCacheIdentifier = getShieldCacheIdentifier(hand);
        cachedPoseContainer.register(shieldCacheIdentifier, shieldStateMachine, true);
        return getShieldCachedPoseFunction(cachedPoseContainer, hand);
    }

    public static PoseFunction<LocalSpacePose> constructWithHandsOffsetByShield(
            CachedPoseContainer cachedPoseContainer,
            PoseFunction<LocalSpacePose> inputPose
    ) {
        PoseFunction<LocalSpacePose> pose = inputPose;
        for (InteractionHand hand : InteractionHand.values()) {
            BlendMask mask = FirstPersonJointAnimator.LEFT_SIDE_MASK;
            pose = constructWithHandOffsetByShield(cachedPoseContainer, pose, hand, mask);
        }
        return pose;
    }

    public static PoseFunction<LocalSpacePose> constructWithHandOffsetByShield(
            CachedPoseContainer cachedPoseContainer,
            PoseFunction<LocalSpacePose> inputPose,
            InteractionHand hand,
            BlendMask handMask
    ) {
        PoseFunction<LocalSpacePose> shieldStateMachine = getShieldCachedPoseFunction(cachedPoseContainer, hand);
        PoseFunction<LocalSpacePose> baseShieldPose;
        baseShieldPose = SequenceEvaluatorFunction.builder(FirstPersonAnimationSequences.HAND_SHIELD_POSE).build();

        PoseFunction<LocalSpacePose> additiveShieldStateMachine;
        additiveShieldStateMachine = MakeDynamicAdditiveFunction.of(shieldStateMachine, baseShieldPose);
        additiveShieldStateMachine = BlendPosesFunction.builder(EmptyPoseFunction.of(false))
                .addBlendInput(additiveShieldStateMachine, evaluationState -> 1f, handMask)
                .build();

        if (hand == InteractionHand.OFF_HAND) {
            additiveShieldStateMachine = MirrorFunction.of(additiveShieldStateMachine);
        }

        PoseFunction<LocalSpacePose> inputWithAdditivePose;
        inputWithAdditivePose = ApplyAdditiveFunction.of(inputPose, additiveShieldStateMachine);
        return inputWithAdditivePose;
    }

    private static PoseFunction<LocalSpacePose> constructShieldStateMachine(
            CachedPoseContainer cachedPoseContainer,
            InteractionHand hand
    ) {
        Predicate<StateTransition.TransitionContext> isUsingShieldPredicate = context -> isUsingShield(context, hand);
        Predicate<StateTransition.TransitionContext> isNotUsingShieldPredicate = isUsingShieldPredicate.negate();

        PoseFunction<LocalSpacePose> shieldStateMachine;
        shieldStateMachine = StateMachineFunction.builder(evaluationState -> ShieldStates.LOWERED)
                .resetsUponRelevant(true)
                .defineState(State.builder(ShieldStates.LOWERED, FirstPersonHandPose.SHIELD.getMiningStateMachine(cachedPoseContainer, hand))
                        .build())
                .defineState(State.builder(ShieldStates.BLOCKING_IN, SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_SHIELD_BLOCK_IN).build())
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(ShieldStates.BLOCKING)
                                .isTakenOnAnimationFinished(1)
                                .setTiming(Transition.builder(TimeSpan.of60FramesPerSecond(5)).build())
                                .build())
                        .build())
                .defineState(State.builder(ShieldStates.BLOCKING, MontageSlotFunction.of(SequenceEvaluatorFunction.builder(FirstPersonAnimationSequences.HAND_SHIELD_BLOCK_OUT).build(), FirstPersonMontages.SHIELD_BLOCK_SLOT))
                        .resetsPoseFunctionUponEntry(true)
                        .build())
                .defineState(State.builder(ShieldStates.BLOCKING_OUT, SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_SHIELD_BLOCK_OUT).build())
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(ShieldStates.LOWERED)
                                .isTakenOnAnimationFinished(1)
                                .setTiming(Transition.builder(TimeSpan.of60FramesPerSecond(15)).build())
                                .build())
                        .build())
                .defineState(State.builder(ShieldStates.DISABLED_IN, SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_SHIELD_DISABLE_IN).build())
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(ShieldStates.DISABLED)
                                .isTakenOnAnimationFinished(0)
                                .setTiming(Transition.SINGLE_TICK)
                                .build())
                        .build())
                .defineState(State.builder(ShieldStates.DISABLED, SequenceEvaluatorFunction.builder(FirstPersonAnimationSequences.HAND_SHIELD_DISABLE_OUT).build())
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(ShieldStates.DISABLED_OUT)
                                .isTakenIfTrue(context -> isShieldNotOnCooldown(context, hand))
                                .setTiming(Transition.SINGLE_TICK)
                                .build())
                        .build())
                .defineState(State.builder(ShieldStates.DISABLED_OUT, SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_SHIELD_DISABLE_OUT).build())
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(ShieldStates.LOWERED)
                                .isTakenOnAnimationFinished(1)
                                .setTiming(Transition.builder(TimeSpan.of60FramesPerSecond(20)).build())
                                .build())
                        .build())
                .addStateAlias(StateAlias.builder(
                                Set.of(
                                        ShieldStates.BLOCKING_IN,
                                        ShieldStates.BLOCKING,
                                        ShieldStates.BLOCKING_OUT,
                                        ShieldStates.LOWERED,
                                        ShieldStates.DISABLED_OUT
                                ))
                        .addOutboundTransition(StateTransition.builder(ShieldStates.DISABLED_IN)
                                .isTakenIfTrue(context -> hasShieldEnteredCooldown(context, hand))
                                .setTiming(Transition.SINGLE_TICK)
                                .build())
                        .build())
                .addStateAlias(StateAlias.builder(
                                Set.of(
                                        ShieldStates.BLOCKING_IN,
                                        ShieldStates.BLOCKING
                                ))
                        .addOutboundTransition(StateTransition.builder(ShieldStates.BLOCKING_OUT)
                                .isTakenIfTrue(isNotUsingShieldPredicate
                                        .and(StateTransition.CURRENT_TRANSITION_FINISHED)
                                )
                                .setTiming(Transition.builder(TimeSpan.of60FramesPerSecond(6)).build())
                                .setPriority(50)
                                .build())
                        .build())
                .addStateAlias(StateAlias.builder(
                                Set.of(
                                        ShieldStates.BLOCKING_IN,
                                        ShieldStates.BLOCKING,
                                        ShieldStates.BLOCKING_OUT
                                ))
                        .addOutboundTransition(StateTransition.builder(ShieldStates.LOWERED)
                                .isTakenIfTrue(isNotUsingShieldPredicate
                                        .and(StateTransition.CURRENT_TRANSITION_FINISHED)
                                        .and(StateTransition.takeIfBooleanDriverTrue(FirstPersonDrivers.IS_MINING))
                                )
                                .setTiming(Transition.builder(TimeSpan.of60FramesPerSecond(6)).build())
                                .setPriority(60)
                                .build())
                        .build())
                .addStateAlias(StateAlias.builder(
                                Set.of(
                                        ShieldStates.BLOCKING_OUT,
                                        ShieldStates.DISABLED_OUT
                                ))
                        .addOutboundTransition(StateTransition.builder(ShieldStates.BLOCKING_IN)
                                .isTakenIfTrue(isUsingShieldPredicate
                                        .and(StateTransition.CURRENT_TRANSITION_FINISHED))
                                .setTiming(Transition.builder(TimeSpan.of60FramesPerSecond(13)).setEasement(Easing.SINE_IN_OUT).build())
                                .build())
                        .build())
                .addStateAlias(StateAlias.builder(
                                Set.of(
                                        ShieldStates.LOWERED
                                ))
                        .addOutboundTransition(StateTransition.builder(ShieldStates.BLOCKING_IN)
                                .isTakenIfTrue(isUsingShieldPredicate)
                                .setTiming(Transition.builder(TimeSpan.of60FramesPerSecond(13)).setEasement(Easing.SINE_IN_OUT).build())
                                .build())
                        .build())
                .build();

        return shieldStateMachine;
    }

    enum ShieldStates {
        LOWERED,
        BLOCKING_IN,
        BLOCKING,
        BLOCKING_OUT,
        DISABLED_IN,
        DISABLED,
        DISABLED_OUT
    }
}