package com.trainguy9512.locomotion.animation.animator.entity.firstperson;

import com.trainguy9512.locomotion.animation.joint.JointChannel;
import com.trainguy9512.locomotion.animation.pose.LocalSpacePose;
import com.trainguy9512.locomotion.animation.pose.function.JointTransformerFunction;
import com.trainguy9512.locomotion.animation.pose.function.PoseFunction;
import com.trainguy9512.locomotion.animation.pose.function.SequenceEvaluatorFunction;
import com.trainguy9512.locomotion.animation.pose.function.SequencePlayerFunction;
import com.trainguy9512.locomotion.animation.pose.function.cache.CachedPoseContainer;
import com.trainguy9512.locomotion.animation.pose.function.statemachine.StateDefinition;
import com.trainguy9512.locomotion.animation.pose.function.statemachine.StateMachineFunction;
import com.trainguy9512.locomotion.animation.pose.function.statemachine.StateTransition;
import com.trainguy9512.locomotion.util.Easing;
import com.trainguy9512.locomotion.util.TimeSpan;
import com.trainguy9512.locomotion.util.Transition;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemUseAnimation;
import org.joml.Vector3f;

public class FirstPersonSpyglass {

    public static final String SPYGLASS_IDLE_STATE = "idle";
    public static final String SPYGLASS_LOOKING_STATE = "looking";
    public static final String SPYGLASS_LOOKING_EXIT_STATE = "looking_exit";

    private static String getSpyglassEntryState(PoseFunction.FunctionEvaluationState evaluationState) {
        return SPYGLASS_IDLE_STATE;
    }

    private static boolean isUsingItem(StateTransition.TransitionContext context, InteractionHand hand) {
        return context.driverContainer().getDriverValue(FirstPersonDrivers.getUsingItemDriver(hand));
    }

    public static PoseFunction<LocalSpacePose> handSpyglassPoseFunction(
            CachedPoseContainer cachedPoseContainer,
            InteractionHand hand
    ) {
        PoseFunction<LocalSpacePose> idlePoseFunction = FirstPersonMining.constructMainHandPickaxeMiningPoseFunction(cachedPoseContainer, hand);
        PoseFunction<LocalSpacePose> lookingPoseFunction = SequenceEvaluatorFunction.builder(FirstPersonAnimationSequences.HAND_SPYGLASS_LOOKING_EXIT).build();
        PoseFunction<LocalSpacePose> lookingExitPoseFunction = SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_SPYGLASS_LOOKING_EXIT)
                .setPlayRate(1)
                .setLooping(false)
                .build();

        return StateMachineFunction.builder(FirstPersonSpyglass::getSpyglassEntryState)
                .defineState(StateDefinition.builder(SPYGLASS_IDLE_STATE, idlePoseFunction)
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(SPYGLASS_LOOKING_STATE)
                                .isTakenIfTrue(context -> isUsingItem(context, hand))
                                .setTiming(Transition.INSTANT)
                                .build())
                        .build())
                .defineState(StateDefinition.builder(SPYGLASS_LOOKING_STATE, lookingPoseFunction)
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(SPYGLASS_LOOKING_EXIT_STATE)
                                .isTakenIfTrue(context -> !isUsingItem(context, hand))
                                .setTiming(Transition.INSTANT)
                                .build())
                        .build())
                .defineState(StateDefinition.builder(SPYGLASS_LOOKING_EXIT_STATE, lookingExitPoseFunction)
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(SPYGLASS_IDLE_STATE)
                                .isTakenOnAnimationFinished(1)
                                .setTiming(Transition.builder(TimeSpan.of60FramesPerSecond(20))
                                        .setEasement(Easing.CUBIC_IN_OUT)
                                        .build())
                                .build())
                        .addOutboundTransition(StateTransition.builder(SPYGLASS_LOOKING_STATE)
                                .isTakenIfTrue(context -> isUsingItem(context, hand))
                                .setTiming(Transition.INSTANT)
                                .build())
                        .build())
                .build();
    }

    /**
     * If the item the player is currently using is a spyglass, return a vector with 0. Otherwise return 1.
     */
    private static Vector3f getHiddenScale(PoseFunction.FunctionInterpolationContext context) {
        for (InteractionHand hand : InteractionHand.values()) {
            if (context
                    .driverContainer()
                    .getInterpolatedDriverValue(
                            FirstPersonDrivers.getUsingItemDriver(hand),
                            context.partialTicks()
                    )
            ) {
                ItemUseAnimation itemUseAnimation = context
                        .driverContainer()
                        .getInterpolatedDriverValue(
                                FirstPersonDrivers.getItemDriver(hand),
                                context.partialTicks()
                        ).getUseAnimation();
                if (itemUseAnimation == ItemUseAnimation.SPYGLASS) {
                    return new Vector3f(0);
                }
            }
        }
        return new Vector3f(1);
    }

    public static PoseFunction<LocalSpacePose> getHiddenArmsSpyglassPose(PoseFunction<LocalSpacePose> inputPoseFunction) {
        return JointTransformerFunction.localOrParentSpaceBuilder(inputPoseFunction, FirstPersonJointAnimator.ARM_BUFFER_JOINT)
                .setScale(FirstPersonSpyglass::getHiddenScale, JointChannel.TransformType.ADD, JointChannel.TransformSpace.LOCAL)
                .build();
    }
}
