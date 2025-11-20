package com.trainguy9512.locomotion.animation.animator.entity.firstperson;

import com.trainguy9512.locomotion.animation.driver.DriverKey;
import com.trainguy9512.locomotion.animation.driver.VariableDriver;
import com.trainguy9512.locomotion.animation.pose.LocalSpacePose;
import com.trainguy9512.locomotion.animation.pose.function.PoseFunction;
import com.trainguy9512.locomotion.animation.pose.function.SequenceEvaluatorFunction;
import com.trainguy9512.locomotion.animation.pose.function.SequencePlayerFunction;
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

public class FirstPersonShield {

    public static PoseFunction<LocalSpacePose> handShieldPoseFunction(CachedPoseContainer cachedPoseContainer, InteractionHand interactionHand) {
        DriverKey<VariableDriver<Boolean>> usingItemDriverKey = FirstPersonDrivers.getUsingItemDriver(interactionHand);
        DriverKey<VariableDriver<Boolean>> isHandOnCooldownKey = FirstPersonDrivers.getItemOnCooldownDriver(interactionHand);
        PoseFunction<LocalSpacePose> shieldBlockingStateMachine = StateMachineFunction.builder(evaluationState -> ShieldStates.LOWERED)
                .resetsUponRelevant(true)
                .defineState(State.builder(ShieldStates.LOWERED, FirstPersonHandPose.SHIELD.getMiningStateMachine(cachedPoseContainer, interactionHand))
                        .build())
                .defineState(State.builder(ShieldStates.BLOCKING_IN, SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_SHIELD_BLOCK_IN).build())
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(ShieldStates.BLOCKING)
                                .isTakenIfMostRelevantAnimationPlayerFinishing(1)
                                .setTiming(Transition.builder(TimeSpan.of60FramesPerSecond(5)).build())
                                .build())
                        .build())
                .defineState(State.builder(ShieldStates.BLOCKING, MontageSlotFunction.of(SequenceEvaluatorFunction.builder(FirstPersonAnimationSequences.HAND_SHIELD_BLOCK_OUT).build(), FirstPersonMontages.SHIELD_BLOCK_SLOT))
                        .resetsPoseFunctionUponEntry(true)
                        .build())
                .defineState(State.builder(ShieldStates.BLOCKING_OUT, SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_SHIELD_BLOCK_OUT).build())
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(ShieldStates.LOWERED)
                                .isTakenIfMostRelevantAnimationPlayerFinishing(1)
                                .setTiming(Transition.builder(TimeSpan.of60FramesPerSecond(15)).build())
                                .build())
                        .build())
                .defineState(State.builder(ShieldStates.DISABLED_IN, SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_SHIELD_DISABLE_IN).build())
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(ShieldStates.DISABLED)
                                .isTakenIfMostRelevantAnimationPlayerFinishing(0)
                                .setTiming(Transition.SINGLE_TICK)
                                .build())
                        .build())
                .defineState(State.builder(ShieldStates.DISABLED, SequenceEvaluatorFunction.builder(FirstPersonAnimationSequences.HAND_SHIELD_DISABLE_OUT).build())
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(ShieldStates.DISABLED_OUT)
                                .isTakenIfTrue(StateTransition.booleanDriverPredicate(isHandOnCooldownKey).negate())
                                .setTiming(Transition.SINGLE_TICK)
                                .build())
                        .build())
                .defineState(State.builder(ShieldStates.DISABLED_OUT, SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_SHIELD_DISABLE_OUT).build())
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(ShieldStates.LOWERED)
                                .isTakenIfMostRelevantAnimationPlayerFinishing(1)
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
                                .isTakenIfTrue(StateTransition.booleanDriverPredicate(isHandOnCooldownKey).and(transitionContext -> transitionContext.driverContainer().getDriver(usingItemDriverKey).getPreviousValue()))
                                .setTiming(Transition.SINGLE_TICK)
                                .build())
                        .build())
                .addStateAlias(StateAlias.builder(
                                Set.of(
                                        ShieldStates.BLOCKING_IN,
                                        ShieldStates.BLOCKING
                                ))
                        .addOutboundTransition(StateTransition.builder(ShieldStates.BLOCKING_OUT)
                                .isTakenIfTrue(StateTransition.booleanDriverPredicate(usingItemDriverKey).negate()
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
                                .isTakenIfTrue(StateTransition.booleanDriverPredicate(usingItemDriverKey).negate()
                                        .and(StateTransition.CURRENT_TRANSITION_FINISHED)
                                        .and(StateTransition.booleanDriverPredicate(FirstPersonDrivers.IS_MINING))
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
                                .isTakenIfTrue(StateTransition.booleanDriverPredicate(usingItemDriverKey)
                                        .and(StateTransition.CURRENT_TRANSITION_FINISHED))
                                .setTiming(Transition.builder(TimeSpan.of60FramesPerSecond(13)).setEasement(Easing.SINE_IN_OUT).build())
                                .build())
                        .build())
                .addStateAlias(StateAlias.builder(
                                Set.of(
                                        ShieldStates.LOWERED
                                ))
                        .addOutboundTransition(StateTransition.builder(ShieldStates.BLOCKING_IN)
                                .isTakenIfTrue(StateTransition.booleanDriverPredicate(usingItemDriverKey))
                                .setTiming(Transition.builder(TimeSpan.of60FramesPerSecond(13)).setEasement(Easing.SINE_IN_OUT).build())
                                .build())
                        .build())
                .build();
        return shieldBlockingStateMachine;
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