package com.trainguy9512.locomotion.animation.animator.entity.firstperson;

import com.trainguy9512.locomotion.animation.driver.DriverKey;
import com.trainguy9512.locomotion.animation.driver.VariableDriver;
import com.trainguy9512.locomotion.animation.pose.LocalSpacePose;
import com.trainguy9512.locomotion.animation.pose.function.PoseFunction;
import com.trainguy9512.locomotion.animation.pose.function.SequencePlayerFunction;
import com.trainguy9512.locomotion.animation.pose.function.cache.CachedPoseContainer;
import com.trainguy9512.locomotion.animation.pose.function.statemachine.StateDefinition;
import com.trainguy9512.locomotion.animation.pose.function.statemachine.StateAlias;
import com.trainguy9512.locomotion.animation.pose.function.statemachine.StateMachineFunction;
import com.trainguy9512.locomotion.animation.pose.function.statemachine.StateTransition;
import com.trainguy9512.locomotion.util.Easing;
import com.trainguy9512.locomotion.util.TimeSpan;
import com.trainguy9512.locomotion.util.Transition;
import net.minecraft.world.InteractionHand;

import java.util.Set;

public class FirstPersonTrident {

    public static final String TRIDENT_IDLE_STATE = "idle";
    public static final String TRIDENT_CHARGE_THROW_STATE = "charge_throw";
    public static final String TRIDENT_RIPTIDE_STATE = "riptide";
    public static final String TRIDENT_RIPTIDE_END_STATE = "riptide_end";

    private static String getTridentEntryState(PoseFunction.FunctionEvaluationState evaluationState) {
        return TRIDENT_IDLE_STATE;
    }

    public static PoseFunction<LocalSpacePose> handTridentPoseFunction(CachedPoseContainer cachedPoseContainer, InteractionHand hand) {
        DriverKey<VariableDriver<Boolean>> usingItemDriverKey = FirstPersonDrivers.getUsingItemDriver(hand);

        PoseFunction<LocalSpacePose> tridentStateMachine;
        tridentStateMachine = StateMachineFunction.builder(FirstPersonTrident::getTridentEntryState)
                .resetsUponRelevant(true)
                .defineState(StateDefinition.builder(TRIDENT_IDLE_STATE, FirstPersonMining.constructMainHandPickaxeMiningPoseFunction(cachedPoseContainer, hand))
                        .resetsPoseFunctionUponEntry(true)
                        .build())
                .defineState(StateDefinition.builder(TRIDENT_CHARGE_THROW_STATE, SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_TRIDENT_CHARGE_THROW).build())
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(TRIDENT_RIPTIDE_END_STATE)
                                .isTakenIfTrue(StateTransition.takeIfBooleanDriverTrue(usingItemDriverKey).negate())
                                .setTiming(Transition.builder(TimeSpan.of60FramesPerSecond(15)).setEasement(Easing.SINE_IN_OUT).build())
                                .setPriority(50)
                                .build())
                        .build())
                .defineState(StateDefinition.builder(TRIDENT_RIPTIDE_STATE, SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_TRIDENT_RIPTIDE).build())
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(TRIDENT_RIPTIDE_END_STATE)
                                .isTakenOnAnimationFinished(1)
                                .isTakenIfTrue(StateTransition.takeIfBooleanDriverTrue(FirstPersonDrivers.IS_IN_RIPTIDE).negate())
                                .setTiming(Transition.builder(TimeSpan.of60FramesPerSecond(8)).setEasement(Easing.SINE_IN_OUT).build())
                                .build())
                        .build())
                .defineState(StateDefinition.builder(TRIDENT_RIPTIDE_END_STATE, SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_TRIDENT_RIPTIDE_END).build())
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(TRIDENT_IDLE_STATE)
                                .isTakenOnAnimationFinished(1)
                                .setTiming(Transition.builder(TimeSpan.of60FramesPerSecond(8)).setEasement(Easing.SINE_IN_OUT).build())
                                .build())
                        .build())
                .addStateAlias(StateAlias.builder(Set.of(
                                TRIDENT_IDLE_STATE,
                                TRIDENT_RIPTIDE_END_STATE,
                                TRIDENT_RIPTIDE_STATE
                        ))
                        .addOutboundTransition(StateTransition.builder(TRIDENT_CHARGE_THROW_STATE)
                                .isTakenIfTrue(StateTransition.takeIfBooleanDriverTrue(usingItemDriverKey))
                                .setTiming(Transition.builder(TimeSpan.of60FramesPerSecond(10)).setEasement(Easing.SINE_IN_OUT).build())
                                .build())
                        .build())
                .addStateAlias(StateAlias.builder(Set.of(
                                TRIDENT_IDLE_STATE,
                                TRIDENT_RIPTIDE_END_STATE
                        ))
                        .addOutboundTransition(StateTransition.builder(TRIDENT_RIPTIDE_STATE)
                                .isTakenIfTrue(context -> shouldPlayRiptideAnimation(context, hand))
                                .setTiming(Transition.builder(TimeSpan.of60FramesPerSecond(2)).setEasement(Easing.SINE_IN_OUT).build())
                                .setPriority(60)
                                .build())
                        .build())
                .build();

        return tridentStateMachine;
    }

    private static boolean shouldPlayRiptideAnimation(StateTransition.TransitionContext context, InteractionHand hand) {
        boolean isInRiptide = context.driverContainer().getDriverValue(FirstPersonDrivers.IS_IN_RIPTIDE);
        InteractionHand lastUsedHand = context.driverContainer().getDriverValue(FirstPersonDrivers.LAST_USED_HAND);
        return isInRiptide && lastUsedHand == hand;
    }
}
