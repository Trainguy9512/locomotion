package com.trainguy9512.locomotion.animation.animator.entity.firstperson;

import com.trainguy9512.locomotion.animation.pose.LocalSpacePose;
import com.trainguy9512.locomotion.animation.pose.function.PoseFunction;
import com.trainguy9512.locomotion.animation.pose.function.SequencePlayerFunction;
import com.trainguy9512.locomotion.animation.pose.function.cache.CachedPoseContainer;
import com.trainguy9512.locomotion.animation.pose.function.statemachine.State;
import com.trainguy9512.locomotion.animation.pose.function.statemachine.StateAlias;
import com.trainguy9512.locomotion.animation.pose.function.statemachine.StateMachineFunction;
import com.trainguy9512.locomotion.animation.pose.function.statemachine.StateTransition;
import com.trainguy9512.locomotion.util.Easing;
import com.trainguy9512.locomotion.util.TimeSpan;
import com.trainguy9512.locomotion.util.Transition;
import net.minecraft.world.InteractionHand;

import java.util.Set;

public class FirstPersonSword {

    public static boolean shouldPlayComboAnimation(StateTransition.TransitionContext context) {
        return context.timeElapsedInCurrentState().inSeconds() > 0.2 && context.timeElapsedInCurrentState().inSeconds() < 0.7;
    }

    public static PoseFunction<LocalSpacePose> handSwordPoseFunction(CachedPoseContainer cachedPoseContainer, InteractionHand interactionHand) {
        return switch (interactionHand) {
            case MAIN_HAND -> StateMachineFunction.builder(evaluationState -> SwordSwingStates.IDLE)
                    .resetsUponRelevant(true)
                    .defineState(State.builder(SwordSwingStates.IDLE, FirstPersonHandPose.SWORD.getMiningStateMachine(cachedPoseContainer, interactionHand))
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(SwordSwingStates.SWING_DOWN)
                                .isTakenIfTrue(StateTransition.takeIfBooleanDriverTrue(FirstPersonDrivers.HAS_ATTACKED))
                                .setTiming(Transition.builder(TimeSpan.ofTicks(1)).build())
                                .build())
                        .build())
                    .defineState(State.builder(SwordSwingStates.SWING_DOWN, SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_TOOL_SWORD_SWING_DOWN).setResetStartTimeOffset(TimeSpan.of60FramesPerSecond(0)).build())
                            .resetsPoseFunctionUponEntry(true)
                            .addOutboundTransition(StateTransition.builder(SwordSwingStates.SWING_RIGHT)
                                    .isTakenIfTrue(StateTransition.takeIfBooleanDriverTrue(FirstPersonDrivers.HAS_ATTACKED).and(FirstPersonSword::shouldPlayComboAnimation))
                                    .setTiming(Transition.builder(TimeSpan.ofTicks(2)).build())
                                    .setPriority(70)
                                    .build())
                            .addOutboundTransition(StateTransition.builder(SwordSwingStates.SWING_DOWN_ALT)
                                    .isTakenIfTrue(StateTransition.takeIfBooleanDriverTrue(FirstPersonDrivers.HAS_ATTACKED))
                                    .setTiming(Transition.builder(TimeSpan.ofTicks(2)).build())
                                    .setPriority(60)
                                    .build())
                            .build())
                    .defineState(State.builder(SwordSwingStates.SWING_DOWN_ALT, SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_TOOL_SWORD_SWING_DOWN).setResetStartTimeOffset(TimeSpan.of60FramesPerSecond(0)).build())
                            .resetsPoseFunctionUponEntry(true)
                            .addOutboundTransition(StateTransition.builder(SwordSwingStates.SWING_RIGHT)
                                    .isTakenIfTrue(StateTransition.takeIfBooleanDriverTrue(FirstPersonDrivers.HAS_ATTACKED).and(FirstPersonSword::shouldPlayComboAnimation))
                                    .setTiming(Transition.builder(TimeSpan.ofTicks(2)).build())
                                    .setPriority(70)
                                    .build())
                            .addOutboundTransition(StateTransition.builder(SwordSwingStates.SWING_DOWN)
                                    .isTakenIfTrue(StateTransition.takeIfBooleanDriverTrue(FirstPersonDrivers.HAS_ATTACKED))
                                    .setTiming(Transition.builder(TimeSpan.ofTicks(1)).build())
                                    .setPriority(60)
                                    .build())
                            .build())
                    .defineState(State.builder(SwordSwingStates.SWING_RIGHT, SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_TOOL_SWORD_SWING_RIGHT).setResetStartTimeOffset(TimeSpan.of60FramesPerSecond(10)).build())
                            .resetsPoseFunctionUponEntry(true)
                            .addOutboundTransition(StateTransition.builder(SwordSwingStates.SWING_LEFT)
                                    .isTakenIfTrue(StateTransition.takeIfBooleanDriverTrue(FirstPersonDrivers.HAS_ATTACKED).and(FirstPersonSword::shouldPlayComboAnimation))
                                    .setTiming(Transition.builder(TimeSpan.ofTicks(2)).build())
                                    .setPriority(70)
                                    .build())
                            .addOutboundTransition(StateTransition.builder(SwordSwingStates.SWING_DOWN_ALT)
                                    .isTakenIfTrue(StateTransition.takeIfBooleanDriverTrue(FirstPersonDrivers.HAS_ATTACKED))
                                    .setTiming(Transition.builder(TimeSpan.ofTicks(2)).build())
                                    .setPriority(60)
                                    .build())
                            .build())
                    .defineState(State.builder(SwordSwingStates.SWING_LEFT, SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_TOOL_SWORD_SWING_LEFT).setResetStartTimeOffset(TimeSpan.of60FramesPerSecond(10)).build())
                            .resetsPoseFunctionUponEntry(true)
                            .addOutboundTransition(StateTransition.builder(SwordSwingStates.SWING_DOWN)
                                    .isTakenIfTrue(StateTransition.takeIfBooleanDriverTrue(FirstPersonDrivers.HAS_ATTACKED).and(FirstPersonSword::shouldPlayComboAnimation))
                                    .setTiming(Transition.builder(TimeSpan.ofTicks(1)).build())
                                    .setPriority(70)
                                    .build())
                            .addOutboundTransition(StateTransition.builder(SwordSwingStates.SWING_DOWN_ALT)
                                    .isTakenIfTrue(StateTransition.takeIfBooleanDriverTrue(FirstPersonDrivers.HAS_ATTACKED))
                                    .setTiming(Transition.builder(TimeSpan.ofTicks(2)).build())
                                    .setPriority(60)
                                    .build())
                            .build())
                    .addStateAlias(StateAlias.builder(
                                    Set.of(
                                            SwordSwingStates.SWING_LEFT,
                                            SwordSwingStates.SWING_RIGHT,
                                            SwordSwingStates.SWING_DOWN
                                    ))
                            .addOutboundTransition(StateTransition.builder(SwordSwingStates.IDLE)
                                    .isTakenOnAnimationFinished(0)
                                    .setTiming(Transition.builder(TimeSpan.of60FramesPerSecond(25)).setEasement(Easing.SINE_IN_OUT).build())
                                    .build())
                            .addOutboundTransition(StateTransition.builder(SwordSwingStates.IDLE)
                                    .isTakenIfTrue(StateTransition.takeIfBooleanDriverTrue(FirstPersonDrivers.IS_MINING))
                                    .setTiming(Transition.builder(TimeSpan.of60FramesPerSecond(6)).setEasement(Easing.SINE_IN_OUT).build())
                                    .build())
                            .build())
                    .build();
            case OFF_HAND -> FirstPersonHandPose.SWORD.getMiningStateMachine(cachedPoseContainer, interactionHand);
        };
    }

    enum SwordSwingStates {
        IDLE,
        SWING_DOWN,
        SWING_DOWN_ALT,
        SWING_LEFT,
        SWING_RIGHT
    }
}