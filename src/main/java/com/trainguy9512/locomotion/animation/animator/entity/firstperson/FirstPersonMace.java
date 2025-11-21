package com.trainguy9512.locomotion.animation.animator.entity.firstperson;

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

public class FirstPersonMace {

    private static boolean isFalling(StateTransition.TransitionContext context) {
        if (context.driverContainer().getDriverValue(FirstPersonDrivers.IS_ON_GROUND)) {
            return false;
        }
        return context.driverContainer().getDriverValue(FirstPersonDrivers.VERTICAL_MOVEMENT_SPEED) < -0.5f;
    }

    private static boolean isNoLongerFalling(StateTransition.TransitionContext context) {
        return !isFalling(context);
    }

    public static PoseFunction<LocalSpacePose> handMacePoseFunction(CachedPoseContainer cachedPoseContainer, InteractionHand interactionHand) {
        return switch (interactionHand) {
            case MAIN_HAND -> macePrepareStateMachine(cachedPoseContainer, interactionHand);
            case OFF_HAND -> FirstPersonHandPose.MACE.getMiningStateMachine(cachedPoseContainer, interactionHand);
        };
    }

    public static PoseFunction<LocalSpacePose> macePrepareStateMachine(CachedPoseContainer cachedPoseContainer, InteractionHand interactionHand) {
        PoseFunction<LocalSpacePose> fallAnticipationSequencePlayer = SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_MACE_FALL_ANTICIPATION)
                .setLooping(false)
                .setPlayRate(1)
                .build();

        return StateMachineFunction.builder(evaluationState -> MacePrepareStates.IDLE)
                .resetsUponRelevant(true)
                .defineState(State.builder(MacePrepareStates.IDLE, FirstPersonHandPose.MACE.getMiningStateMachine(cachedPoseContainer, interactionHand))
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(MacePrepareStates.FALLING)
                                .isTakenIfTrue(FirstPersonMace::isFalling)
                                .setTiming(Transition.builder(TimeSpan.ofSeconds(0.25f))
                                        .setEasement(Easing.CUBIC_IN_OUT)
                                        .build())
                                .setCanInterruptOtherTransitions(false)
                                .build())
                        .build())
                .defineState(State.builder(MacePrepareStates.FALLING, fallAnticipationSequencePlayer)
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(MacePrepareStates.IDLE)
                                .isTakenIfTrue(FirstPersonMace::isNoLongerFalling)
                                .setTiming(Transition.builder(TimeSpan.ofSeconds(0.4f))
                                        .setEasement(Easing.EXPONENTIAL_OUT)
                                        .build())
                                .setCanInterruptOtherTransitions(false)
                                .build())
                        .build())
                .build();
    }

    enum MacePrepareStates {
        IDLE,
        FALLING,
    }
}