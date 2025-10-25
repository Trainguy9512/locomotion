package com.trainguy9512.locomotion.animation.animator.entity.firstperson;

import com.trainguy9512.locomotion.animation.driver.DriverKey;
import com.trainguy9512.locomotion.animation.driver.VariableDriver;
import com.trainguy9512.locomotion.animation.pose.LocalSpacePose;
import com.trainguy9512.locomotion.animation.pose.function.PoseFunction;
import com.trainguy9512.locomotion.animation.pose.function.SequencePlayerFunction;
import com.trainguy9512.locomotion.animation.pose.function.cache.CachedPoseContainer;
import com.trainguy9512.locomotion.animation.pose.function.statemachine.State;
import com.trainguy9512.locomotion.animation.pose.function.statemachine.StateMachineFunction;
import com.trainguy9512.locomotion.animation.pose.function.statemachine.StateTransition;
import com.trainguy9512.locomotion.util.Easing;
import com.trainguy9512.locomotion.util.TimeSpan;
import com.trainguy9512.locomotion.util.Transition;
import net.minecraft.world.InteractionHand;

public class FirstPersonTrident {

    public static PoseFunction<LocalSpacePose> handTridentPoseFunction(CachedPoseContainer cachedPoseContainer, InteractionHand interactionHand) {
        DriverKey<VariableDriver<Boolean>> usingItemDriverKey = FirstPersonDrivers.getUsingItemDriver(interactionHand);

        PoseFunction<LocalSpacePose> tridentStateMachine = StateMachineFunction.builder(evaluationState -> TridentStates.IDLE)
                .resetsUponRelevant(true)
                .defineState(State.builder(TridentStates.IDLE, FirstPersonHandPose.TRIDENT.getMiningStateMachine(cachedPoseContainer, interactionHand))
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(TridentStates.CHARGE_THROW)
                                .isTakenIfTrue(StateTransition.booleanDriverPredicate(usingItemDriverKey))
                                .setTiming(Transition.builder(TimeSpan.of60FramesPerSecond(10)).setEasement(Easing.SINE_IN_OUT).build())
                                .build())
                        .build())
                .defineState(State.builder(TridentStates.CHARGE_THROW, SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_TRIDENT_CHARGE_THROW).build())
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(TridentStates.RIPTIDE)
                                .isTakenIfTrue(StateTransition.booleanDriverPredicate(usingItemDriverKey).negate().and(StateTransition.booleanDriverPredicate(FirstPersonDrivers.IS_IN_RIPTIDE)))
                                .setTiming(Transition.builder(TimeSpan.of60FramesPerSecond(2)).setEasement(Easing.SINE_IN_OUT).build())
                                .setPriority(60)
                                .build())
                        .addOutboundTransition(StateTransition.builder(TridentStates.IDLE)
                                .isTakenIfTrue(context -> shouldCancelChargeThrow(context, interactionHand))
                                .setTiming(Transition.builder(TimeSpan.ofSeconds(0.25f)).setEasement(Easing.SINE_OUT).build())
                                .setPriority(50)
                                .build())
                        .build())
                .defineState(State.builder(TridentStates.RIPTIDE, SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_TRIDENT_RIPTIDE).build())
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(TridentStates.IDLE)
                                .isTakenIfMostRelevantAnimationPlayerFinishing(1)
                                .setTiming(Transition.builder(TimeSpan.of60FramesPerSecond(25)).setEasement(Easing.SINE_IN_OUT).build())
                                .build())
                        .build())
                .build();

        return tridentStateMachine;
    }

    enum TridentStates {
        IDLE,
        CHARGE_THROW,
        RIPTIDE
    }

    public static boolean shouldCancelChargeThrow(StateTransition.TransitionContext context, InteractionHand interactionHand) {
        DriverKey<VariableDriver<Boolean>> usingItemDriverKey = FirstPersonDrivers.getUsingItemDriver(interactionHand);
        return !context.driverContainer().getDriver(usingItemDriverKey).getPreviousValue();
    }
}
