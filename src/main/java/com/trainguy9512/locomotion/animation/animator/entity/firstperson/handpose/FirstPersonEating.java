package com.trainguy9512.locomotion.animation.animator.entity.firstperson.handpose;

import com.trainguy9512.locomotion.animation.animator.entity.firstperson.FirstPersonAnimationSequences;
import com.trainguy9512.locomotion.animation.animator.entity.firstperson.FirstPersonDrivers;
import com.trainguy9512.locomotion.animation.data.DriverGetter;
import com.trainguy9512.locomotion.animation.pose.LocalSpacePose;
import com.trainguy9512.locomotion.animation.pose.function.ApplyAdditiveFunction;
import com.trainguy9512.locomotion.animation.pose.function.PoseFunction;
import com.trainguy9512.locomotion.animation.pose.function.SequencePlayerFunction;
import com.trainguy9512.locomotion.animation.pose.function.SequenceReferencePoint;
import com.trainguy9512.locomotion.animation.pose.function.cache.CachedPoseContainer;
import com.trainguy9512.locomotion.animation.pose.function.statemachine.*;
import com.trainguy9512.locomotion.animation.util.Easing;
import com.trainguy9512.locomotion.animation.util.TimeSpan;
import com.trainguy9512.locomotion.animation.util.Transition;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemUseAnimation;

import java.util.Set;

public class FirstPersonEating {

    public static final String EATING_IDLE_STATE = "idle";
    public static final String EATING_BEGIN_STATE = "eating_begin";
    public static final String EATING_LOOP_STATE = "eating_loop";

    public static PoseFunction<LocalSpacePose> constructWithEatingStateMachine(CachedPoseContainer cachedPoseContainer, InteractionHand hand, PoseFunction<LocalSpacePose> idlePoseFunction) {
        PoseFunction<LocalSpacePose> drinkingLoopPoseFunction = ApplyAdditiveFunction.of(
                SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_GENERIC_ITEM_DRINK_PROGRESS)
                        .setPlayRate(context -> context.getDriverValue(FirstPersonDrivers.ITEM_CONSUMPTION_SPEED))
                        .build(),
                SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_GENERIC_ITEM_DRINK_LOOP)
                        .setLooping(true)
                        .setPlayRate(1f)
                        .isAdditive(true, SequenceReferencePoint.BEGINNING)
                        .build()
        );

        // Eating pose functions
        PoseFunction<LocalSpacePose> eatingLoopPoseFunction = SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_GENERIC_ITEM_EAT_LOOP)
                .setPlayRate(1.5f)
                .setLooping(true)
                .build();
        PoseFunction<LocalSpacePose> eatingBeginPoseFunction = SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_GENERIC_ITEM_EAT_BEGIN).build();

        return StateMachineFunction.builder(context -> EATING_IDLE_STATE)
                .resetsUponRelevant(true)
                .defineState(StateDefinition.builder(EATING_IDLE_STATE, idlePoseFunction)
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(EATING_BEGIN_STATE)
                                .isTakenIfTrue(context -> isEating(context, hand))
                                .build())
                        .build())
                .defineState(StateDefinition.builder(EATING_BEGIN_STATE, eatingBeginPoseFunction)
                        .addOutboundTransition(StateTransition.builder(EATING_LOOP_STATE)
                                .setTiming(Transition.builder(TimeSpan.ofSeconds(0.1f)).setEasement(Easing.SINE_IN_OUT).build())
                                .isTakenOnAnimationFinished(1)
                                .build())
                        .resetsPoseFunctionUponEntry(true)
                        .build())
                .defineState(StateDefinition.builder(EATING_LOOP_STATE, eatingLoopPoseFunction)
                        .resetsPoseFunctionUponEntry(true)
                        .build())
                .addStateAlias(StateAlias.builder(
                                Set.of(
                                        EATING_BEGIN_STATE,
                                        EATING_LOOP_STATE
                                ))
                        .addOutboundTransition(StateTransition.builder(EATING_IDLE_STATE)
                                .isTakenIfTrue(context -> !isEating(context, hand))
                                .setCanInterruptOtherTransitions(false)
                                .setTiming(Transition.builder(TimeSpan.ofSeconds(0.8f))
                                        .setEasement(Easing.Elastic.of(4, true))
                                        .build())
                                .build())
                        .build())
                .build();
    }

    private static boolean isEating(StateTransitionContext context, InteractionHand hand) {
        if (!context.getDriverValue(FirstPersonDrivers.getUsingItemDriver(hand))) {
            return false;
        }
        return context.getDriverValue(FirstPersonDrivers.getRenderedItemDriver(hand)).getUseAnimation() == ItemUseAnimation.EAT;
    }
}
