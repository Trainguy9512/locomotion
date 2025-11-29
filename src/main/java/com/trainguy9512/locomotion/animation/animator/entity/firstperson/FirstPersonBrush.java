package com.trainguy9512.locomotion.animation.animator.entity.firstperson;

import com.trainguy9512.locomotion.animation.pose.LocalSpacePose;
import com.trainguy9512.locomotion.animation.pose.function.PoseFunction;
import com.trainguy9512.locomotion.animation.pose.function.SequencePlayerFunction;
import com.trainguy9512.locomotion.animation.pose.function.cache.CachedPoseContainer;
import com.trainguy9512.locomotion.animation.pose.function.statemachine.StateDefinition;
import com.trainguy9512.locomotion.animation.pose.function.statemachine.StateMachineFunction;
import com.trainguy9512.locomotion.animation.pose.function.statemachine.StateTransition;
import com.trainguy9512.locomotion.util.Easing;
import com.trainguy9512.locomotion.util.TimeSpan;
import com.trainguy9512.locomotion.util.Transition;
import net.minecraft.world.InteractionHand;

public class FirstPersonBrush {

    private static boolean isUsingItem(StateTransition.TransitionContext context, InteractionHand interactionHand) {
        return context.driverContainer().getDriverValue(FirstPersonDrivers.getUsingItemDriver(interactionHand));
    }

    public static final String BRUSH_IDLE_STATE = "idle";
    public static final String BRUSH_SIFTING_STATE = "sifting";

    public static PoseFunction<LocalSpacePose> handBrushPoseFunction(
            CachedPoseContainer cachedPoseContainer,
            InteractionHand interactionHand
    ) {
        PoseFunction<LocalSpacePose> idlePoseFunction = FirstPersonHandPose.BRUSH.getMiningStateMachine(cachedPoseContainer, interactionHand);
        PoseFunction<LocalSpacePose> siftingPoseFunction = SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_BRUSH_SIFT_LOOP)
                .setPlayRate(1)
                .setLooping(true)
                .build();

        return StateMachineFunction.builder(functionEvaluationState -> BRUSH_IDLE_STATE)
                .defineState(StateDefinition.builder(BRUSH_IDLE_STATE, idlePoseFunction)
                        .addOutboundTransition(StateTransition.builder(BRUSH_SIFTING_STATE)
                                .isTakenIfTrue(context -> isUsingItem(context, interactionHand))
                                .setTiming(Transition.builder(TimeSpan.of60FramesPerSecond(8))
                                        .setEasement(Easing.CUBIC_IN_OUT)
                                        .build())
                                .setCanInterruptOtherTransitions(true)
                                .build())
                        .resetsPoseFunctionUponEntry(true)
                        .build())
                .defineState(StateDefinition.builder(BRUSH_SIFTING_STATE, siftingPoseFunction)
                        .addOutboundTransition(StateTransition.builder(BRUSH_IDLE_STATE)
                                .isTakenIfTrue(context -> !isUsingItem(context, interactionHand))
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
