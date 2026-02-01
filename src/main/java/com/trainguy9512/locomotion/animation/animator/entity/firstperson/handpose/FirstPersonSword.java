package com.trainguy9512.locomotion.animation.animator.entity.firstperson.handpose;

import com.trainguy9512.locomotion.animation.animator.entity.firstperson.FirstPersonMining;
import com.trainguy9512.locomotion.animation.pose.LocalSpacePose;
import com.trainguy9512.locomotion.animation.pose.function.PoseFunction;
import com.trainguy9512.locomotion.animation.pose.function.cache.CachedPoseContainer;
import com.trainguy9512.locomotion.animation.pose.function.statemachine.StateTransition;
import net.minecraft.world.InteractionHand;

public class FirstPersonSword {

    public static boolean shouldPlayComboAnimation(StateTransition.TransitionContext context) {
        return context.timeElapsedInCurrentState().inSeconds() > 0.2 && context.timeElapsedInCurrentState().inSeconds() < 0.7;
    }

    public static final String SWORD_IDLE_STATE = "idle";
    public static final String SWORD_SWING_DOWN_STATE = "swing_down";
    public static final String SWORD_SWING_DOWN_ALT_STATE = "swing_down_alt";
    public static final String SWORD_SWING_LEFT_STATE = "swing_left";
    public static final String SWORD_SWING_RIGHT_STATE = "swing_right";

    private static String getSwordEntryState(PoseFunction.FunctionEvaluationState evaluationState) {
        return SWORD_IDLE_STATE;
    }

    public static PoseFunction<LocalSpacePose> handSwordPoseFunction(CachedPoseContainer cachedPoseContainer, InteractionHand hand, PoseFunction<LocalSpacePose> miningPoseFunction) {

        return miningPoseFunction;
//        return switch (hand) {
//            case MAIN_HAND -> StateMachineFunction.builder(FirstPersonSword::getSwordEntryState)
//                    .resetsUponRelevant(true)
//                    .defineState(StateDefinition.builder(SWORD_IDLE_STATE, miningPoseFunction)
//                        .resetsPoseFunctionUponEntry(true)
//                        .addOutboundTransition(StateTransition.builder(SWORD_SWING_DOWN_STATE)
//                                .isTakenIfTrue(StateTransition.takeIfBooleanDriverTrue(FirstPersonDrivers.HAS_ATTACKED))
//                                .setTiming(Transition.builder(TimeSpan.ofTicks(1)).build())
//                                .build())
//                        .build())
//                    .defineState(StateDefinition.builder(SWORD_SWING_DOWN_STATE, SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_TOOL_SWORD_SWING_DOWN).setResetStartTimeOffset(TimeSpan.of60FramesPerSecond(0)).build())
//                            .resetsPoseFunctionUponEntry(true)
//                            .addOutboundTransition(StateTransition.builder(SWORD_SWING_RIGHT_STATE)
//                                    .isTakenIfTrue(StateTransition.takeIfBooleanDriverTrue(FirstPersonDrivers.HAS_ATTACKED).and(FirstPersonSword::shouldPlayComboAnimation))
//                                    .setTiming(Transition.builder(TimeSpan.ofTicks(2)).build())
//                                    .setPriority(70)
//                                    .build())
//                            .addOutboundTransition(StateTransition.builder(SWORD_SWING_DOWN_ALT_STATE)
//                                    .isTakenIfTrue(StateTransition.takeIfBooleanDriverTrue(FirstPersonDrivers.HAS_ATTACKED))
//                                    .setTiming(Transition.builder(TimeSpan.ofTicks(2)).build())
//                                    .setPriority(60)
//                                    .build())
//                            .build())
//                    .defineState(StateDefinition.builder(SWORD_SWING_DOWN_ALT_STATE, SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_TOOL_SWORD_SWING_DOWN).setResetStartTimeOffset(TimeSpan.of60FramesPerSecond(0)).build())
//                            .resetsPoseFunctionUponEntry(true)
//                            .addOutboundTransition(StateTransition.builder(SWORD_SWING_RIGHT_STATE)
//                                    .isTakenIfTrue(StateTransition.takeIfBooleanDriverTrue(FirstPersonDrivers.HAS_ATTACKED).and(FirstPersonSword::shouldPlayComboAnimation))
//                                    .setTiming(Transition.builder(TimeSpan.ofTicks(2)).build())
//                                    .setPriority(70)
//                                    .build())
//                            .addOutboundTransition(StateTransition.builder(SWORD_SWING_DOWN_STATE)
//                                    .isTakenIfTrue(StateTransition.takeIfBooleanDriverTrue(FirstPersonDrivers.HAS_ATTACKED))
//                                    .setTiming(Transition.builder(TimeSpan.ofTicks(1)).build())
//                                    .setPriority(60)
//                                    .build())
//                            .build())
//                    .defineState(StateDefinition.builder(SWORD_SWING_RIGHT_STATE, SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_TOOL_SWORD_SWING_RIGHT).setResetStartTimeOffset(TimeSpan.of60FramesPerSecond(10)).build())
//                            .resetsPoseFunctionUponEntry(true)
//                            .addOutboundTransition(StateTransition.builder(SWORD_SWING_LEFT_STATE)
//                                    .isTakenIfTrue(StateTransition.takeIfBooleanDriverTrue(FirstPersonDrivers.HAS_ATTACKED).and(FirstPersonSword::shouldPlayComboAnimation))
//                                    .setTiming(Transition.builder(TimeSpan.ofTicks(2)).build())
//                                    .setPriority(70)
//                                    .build())
//                            .addOutboundTransition(StateTransition.builder(SWORD_SWING_DOWN_ALT_STATE)
//                                    .isTakenIfTrue(StateTransition.takeIfBooleanDriverTrue(FirstPersonDrivers.HAS_ATTACKED))
//                                    .setTiming(Transition.builder(TimeSpan.ofTicks(2)).build())
//                                    .setPriority(60)
//                                    .build())
//                            .build())
//                    .defineState(StateDefinition.builder(SWORD_SWING_LEFT_STATE, SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_TOOL_SWORD_SWING_LEFT).setResetStartTimeOffset(TimeSpan.of60FramesPerSecond(10)).build())
//                            .resetsPoseFunctionUponEntry(true)
//                            .addOutboundTransition(StateTransition.builder(SWORD_SWING_DOWN_STATE)
//                                    .isTakenIfTrue(StateTransition.takeIfBooleanDriverTrue(FirstPersonDrivers.HAS_ATTACKED).and(FirstPersonSword::shouldPlayComboAnimation))
//                                    .setTiming(Transition.builder(TimeSpan.ofTicks(1)).build())
//                                    .setPriority(70)
//                                    .build())
//                            .addOutboundTransition(StateTransition.builder(SWORD_SWING_DOWN_ALT_STATE)
//                                    .isTakenIfTrue(StateTransition.takeIfBooleanDriverTrue(FirstPersonDrivers.HAS_ATTACKED))
//                                    .setTiming(Transition.builder(TimeSpan.ofTicks(2)).build())
//                                    .setPriority(60)
//                                    .build())
//                            .build())
//                    .addStateAlias(StateAlias.builder(
//                                    Set.of(
//                                            SWORD_SWING_LEFT_STATE,
//                                            SWORD_SWING_RIGHT_STATE,
//                                            SWORD_SWING_DOWN_STATE
//                                    ))
//                            .addOutboundTransition(StateTransition.builder(SWORD_IDLE_STATE)
//                                    .isTakenOnAnimationFinished(0)
//                                    .setTiming(Transition.builder(TimeSpan.of60FramesPerSecond(25)).setEasement(Easing.SINE_IN_OUT).build())
//                                    .build())
//                            .addOutboundTransition(StateTransition.builder(SWORD_IDLE_STATE)
//                                    .isTakenIfTrue(StateTransition.takeIfBooleanDriverTrue(FirstPersonDrivers.IS_MINING))
//                                    .setTiming(Transition.builder(TimeSpan.of60FramesPerSecond(6)).setEasement(Easing.SINE_IN_OUT).build())
//                                    .build())
//                            .build())
//                    .build();
//            case OFF_HAND -> miningPoseFunction;
//        };
    }
}