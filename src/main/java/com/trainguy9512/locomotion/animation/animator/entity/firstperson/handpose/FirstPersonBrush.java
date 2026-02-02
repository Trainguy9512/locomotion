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

public class FirstPersonBrush {

    private static boolean isUsingItem(StateTransitionContext context, InteractionHand hand) {
        return context.driverContainer().getDriverValue(FirstPersonDrivers.getUsingItemDriver(hand));
    }

    public static final String BRUSH_IDLE_STATE = "idle";
    public static final String BRUSH_SIFTING_STATE = "sifting";

    public static PoseFunction<LocalSpacePose> constructBrushPoseFunction(
            CachedPoseContainer cachedPoseContainer,
            InteractionHand hand,
            PoseFunction<LocalSpacePose> miningPoseFunction
    ) {
        PoseFunction<LocalSpacePose> siftingPoseFunction = SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_BRUSH_SIFT_LOOP)
                .setPlayRate(1)
                .setLooping(true)
                .build();

        return StateMachineFunction.builder(functioncontext -> BRUSH_IDLE_STATE)
                .defineState(StateDefinition.builder(BRUSH_IDLE_STATE, miningPoseFunction)
                        .addOutboundTransition(StateTransition.builder(BRUSH_SIFTING_STATE)
                                .isTakenIfTrue(context -> isUsingItem(context, hand))
                                .setTiming(Transition.builder(TimeSpan.of60FramesPerSecond(8))
                                        .setEasement(Easing.CUBIC_IN_OUT)
                                        .build())
                                .setCanInterruptOtherTransitions(true)
                                .build())
                        .resetsPoseFunctionUponEntry(true)
                        .build())
                .defineState(StateDefinition.builder(BRUSH_SIFTING_STATE, siftingPoseFunction)
                        .addOutboundTransition(StateTransition.builder(BRUSH_IDLE_STATE)
                                .isTakenIfTrue(context -> !isUsingItem(context, hand))
                                .setTiming(Transition.builder(TimeSpan.ofSeconds(0.4f))
                                        .setEasement(Easing.EXPONENTIAL_OUT)
                                        .build())
                                .setCanInterruptOtherTransitions(false)
                                .build())
                        .resetsPoseFunctionUponEntry(true)
                        .build())
                .build();
    }
}
