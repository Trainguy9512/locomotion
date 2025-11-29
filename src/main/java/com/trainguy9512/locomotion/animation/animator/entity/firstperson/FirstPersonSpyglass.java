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

    public enum SpyglassStates {
        IDLE,
        LOOKING,
        LOOKING_EXIT
    }

    private static boolean isUsingItem(StateTransition.TransitionContext context, InteractionHand interactionHand) {
        return context.driverContainer().getDriverValue(FirstPersonDrivers.getUsingItemDriver(interactionHand));
    }

    public static PoseFunction<LocalSpacePose> handSpyglassPoseFunction(
            CachedPoseContainer cachedPoseContainer,
            InteractionHand interactionHand
    ) {
        PoseFunction<LocalSpacePose> idlePoseFunction = FirstPersonHandPose.SPYGLASS.getMiningStateMachine(cachedPoseContainer, interactionHand);
        PoseFunction<LocalSpacePose> lookingPoseFunction = SequenceEvaluatorFunction.builder(FirstPersonAnimationSequences.HAND_SPYGLASS_LOOKING_EXIT).build();
        PoseFunction<LocalSpacePose> lookingExitPoseFunction = SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_SPYGLASS_LOOKING_EXIT)
                .setPlayRate(1)
                .setLooping(false)
                .build();

        return StateMachineFunction.builder(functionEvaluationState -> SpyglassStates.IDLE)
                .defineState(StateDefinition.builder(SpyglassStates.IDLE, idlePoseFunction)
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(SpyglassStates.LOOKING)
                                .isTakenIfTrue(context -> isUsingItem(context, interactionHand))
                                .setTiming(Transition.INSTANT)
                                .build())
                        .build())
                .defineState(StateDefinition.builder(SpyglassStates.LOOKING, lookingPoseFunction)
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(SpyglassStates.LOOKING_EXIT)
                                .isTakenIfTrue(context -> !isUsingItem(context, interactionHand))
                                .setTiming(Transition.INSTANT)
                                .build())
                        .build())
                .defineState(StateDefinition.builder(SpyglassStates.LOOKING_EXIT, lookingExitPoseFunction)
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(SpyglassStates.IDLE)
                                .isTakenOnAnimationFinished(1)
                                .setTiming(Transition.builder(TimeSpan.of60FramesPerSecond(20))
                                        .setEasement(Easing.CUBIC_IN_OUT)
                                        .build())
                                .build())
                        .addOutboundTransition(StateTransition.builder(SpyglassStates.LOOKING)
                                .isTakenIfTrue(context -> isUsingItem(context, interactionHand))
                                .setTiming(Transition.INSTANT)
                                .build())
                        .build())
                .build();
    }

    /**
     * If the item the player is currently using is a spyglass, return a vector with 0. Otherwise return 1.
     */
    private static Vector3f getHiddenScale(PoseFunction.FunctionInterpolationContext context) {
        for (InteractionHand interactionHand : InteractionHand.values()) {
            if (context
                    .driverContainer()
                    .getInterpolatedDriverValue(
                            FirstPersonDrivers.getUsingItemDriver(interactionHand),
                            context.partialTicks()
                    )
            ) {
                ItemUseAnimation itemUseAnimation = context
                        .driverContainer()
                        .getInterpolatedDriverValue(
                                FirstPersonDrivers.getItemDriver(interactionHand),
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
