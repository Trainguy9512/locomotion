package com.trainguy9512.locomotion.animation.animator.entity.firstperson.handpose;

import com.trainguy9512.locomotion.animation.animator.entity.firstperson.FirstPersonAnimationSequences;
import com.trainguy9512.locomotion.animation.animator.entity.firstperson.FirstPersonDrivers;
import com.trainguy9512.locomotion.animation.pose.LocalSpacePose;
import com.trainguy9512.locomotion.animation.pose.function.PoseFunction;
import com.trainguy9512.locomotion.animation.pose.function.SequencePlayerFunction;
import com.trainguy9512.locomotion.animation.pose.function.cache.CachedPoseContainer;
import com.trainguy9512.locomotion.animation.pose.function.statemachine.StateDefinition;
import com.trainguy9512.locomotion.animation.pose.function.statemachine.StateMachineFunction;
import com.trainguy9512.locomotion.animation.pose.function.statemachine.StateTransition;
import com.trainguy9512.locomotion.animation.pose.function.statemachine.StateTransitionContext;
import com.trainguy9512.locomotion.animation.util.Easing;
import com.trainguy9512.locomotion.animation.util.TimeSpan;
import com.trainguy9512.locomotion.animation.util.Transition;
import net.minecraft.world.InteractionHand;

public class FirstPersonMace {

    private static boolean isFalling(StateTransitionContext context) {
        if (context.getDriverValue(FirstPersonDrivers.IS_ON_GROUND)) {
            return false;
        }
        return context.getDriverValue(FirstPersonDrivers.VERTICAL_MOVEMENT_SPEED) < -0.5f;
    }

    private static boolean isNoLongerFalling(StateTransitionContext context) {
        return !isFalling(context);
    }

    public static PoseFunction<LocalSpacePose> handMacePoseFunction(
            CachedPoseContainer cachedPoseContainer,
            InteractionHand hand,
            PoseFunction<LocalSpacePose> miningPoseFunction
    ) {
        return switch (hand) {
            case MAIN_HAND -> macePrepareStateMachine(cachedPoseContainer, hand, miningPoseFunction);
            case OFF_HAND -> miningPoseFunction;
        };
    }

    public static final String MACE_PREPARE_IDLE_STATE = "idle";
    public static final String MACE_PREPARE_FALLING_STATE = "falling";

    public static PoseFunction<LocalSpacePose> macePrepareStateMachine(
            CachedPoseContainer cachedPoseContainer,
            InteractionHand hand,
            PoseFunction<LocalSpacePose> miningPoseFunction
    ) {
        PoseFunction<LocalSpacePose> fallAnticipationSequencePlayer = SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_MACE_FALL_ANTICIPATION)
                .setLooping(false)
                .setPlayRate(1)
                .build();

        return StateMachineFunction.builder(context -> MACE_PREPARE_IDLE_STATE)
                .resetsUponRelevant(true)
                .defineState(StateDefinition.builder(MACE_PREPARE_IDLE_STATE, miningPoseFunction)
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(MACE_PREPARE_FALLING_STATE)
                                .isTakenIfTrue(FirstPersonMace::isFalling)
                                .setTiming(Transition.builder(TimeSpan.ofSeconds(0.25f))
                                        .setEasement(Easing.CUBIC_IN_OUT)
                                        .build())
                                .setCanInterruptOtherTransitions(false)
                                .build())
                        .build())
                .defineState(StateDefinition.builder(MACE_PREPARE_FALLING_STATE, fallAnticipationSequencePlayer)
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(MACE_PREPARE_IDLE_STATE)
                                .isTakenIfTrue(FirstPersonMace::isNoLongerFalling)
                                .setTiming(Transition.builder(TimeSpan.ofSeconds(0.4f))
                                        .setEasement(Easing.EXPONENTIAL_OUT)
                                        .build())
                                .setCanInterruptOtherTransitions(false)
                                .build())
                        .build())
                .build();
    }
}