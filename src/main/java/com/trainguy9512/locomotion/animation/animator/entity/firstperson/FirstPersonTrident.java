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

    public static PoseFunction<LocalSpacePose> handTridentPoseFunction(CachedPoseContainer cachedPoseContainer, InteractionHand interactionHand) {
        DriverKey<VariableDriver<Boolean>> usingItemDriverKey = FirstPersonDrivers.getUsingItemDriver(interactionHand);

        PoseFunction<LocalSpacePose> tridentStateMachine = StateMachineFunction.builder(evaluationState -> TridentStates.IDLE)
                .resetsUponRelevant(true)
                .defineState(StateDefinition.builder(TridentStates.IDLE, FirstPersonHandPose.TRIDENT.getMiningStateMachine(cachedPoseContainer, interactionHand))
                        .resetsPoseFunctionUponEntry(true)
                        .build())
                .defineState(StateDefinition.builder(TridentStates.CHARGE_THROW, SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_TRIDENT_CHARGE_THROW).build())
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(TridentStates.RIPTIDE_END)
                                .isTakenIfTrue(StateTransition.takeIfBooleanDriverTrue(usingItemDriverKey).negate())
                                .setTiming(Transition.builder(TimeSpan.of60FramesPerSecond(15)).setEasement(Easing.SINE_IN_OUT).build())
                                .setPriority(50)
                                .build())
                        .build())
                .defineState(StateDefinition.builder(TridentStates.RIPTIDE, SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_TRIDENT_RIPTIDE).build())
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(TridentStates.RIPTIDE_END)
                                .isTakenOnAnimationFinished(1)
                                .isTakenIfTrue(StateTransition.takeIfBooleanDriverTrue(FirstPersonDrivers.IS_IN_RIPTIDE).negate())
                                .setTiming(Transition.builder(TimeSpan.of60FramesPerSecond(8)).setEasement(Easing.SINE_IN_OUT).build())
                                .build())
                        .build())
                .defineState(StateDefinition.builder(TridentStates.RIPTIDE_END, SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_TRIDENT_RIPTIDE_END).build())
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(TridentStates.IDLE)
                                .isTakenOnAnimationFinished(1)
                                .setTiming(Transition.builder(TimeSpan.of60FramesPerSecond(8)).setEasement(Easing.SINE_IN_OUT).build())
                                .build())
                        .build())
                .addStateAlias(StateAlias.builder(Set.of(
                                TridentStates.IDLE,
                                TridentStates.RIPTIDE_END,
                                TridentStates.RIPTIDE
                        ))
                        .addOutboundTransition(StateTransition.builder(TridentStates.CHARGE_THROW)
                                .isTakenIfTrue(StateTransition.takeIfBooleanDriverTrue(usingItemDriverKey))
                                .setTiming(Transition.builder(TimeSpan.of60FramesPerSecond(10)).setEasement(Easing.SINE_IN_OUT).build())
                                .build())
                        .build())
                .addStateAlias(StateAlias.builder(Set.of(
                                TridentStates.IDLE,
                                TridentStates.RIPTIDE_END
                        ))
                        .addOutboundTransition(StateTransition.builder(TridentStates.RIPTIDE)
                                .isTakenIfTrue(context -> shouldPlayRiptideAnimation(context, interactionHand))
                                .setTiming(Transition.builder(TimeSpan.of60FramesPerSecond(2)).setEasement(Easing.SINE_IN_OUT).build())
                                .setPriority(60)
                                .build())
                        .build())
                .build();

        return tridentStateMachine;
    }

    enum TridentStates {
        IDLE,
        CHARGE_THROW,
        RIPTIDE,
        RIPTIDE_END
    }

    private static boolean shouldPlayRiptideAnimation(StateTransition.TransitionContext context, InteractionHand interactionHand) {
        boolean isInRiptide = context.driverContainer().getDriverValue(FirstPersonDrivers.IS_IN_RIPTIDE);
        InteractionHand lastUsedHand = context.driverContainer().getDriverValue(FirstPersonDrivers.LAST_USED_HAND);
        return isInRiptide && lastUsedHand == interactionHand;
    }
}
