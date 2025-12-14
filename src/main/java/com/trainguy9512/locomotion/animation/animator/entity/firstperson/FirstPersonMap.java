package com.trainguy9512.locomotion.animation.animator.entity.firstperson;

import com.trainguy9512.locomotion.LocomotionMain;
import com.trainguy9512.locomotion.animation.joint.skeleton.BlendMask;
import com.trainguy9512.locomotion.animation.pose.LocalSpacePose;
import com.trainguy9512.locomotion.animation.pose.Pose;
import com.trainguy9512.locomotion.animation.pose.function.BlendPosesFunction;
import com.trainguy9512.locomotion.animation.pose.function.EmptyPoseFunction;
import com.trainguy9512.locomotion.animation.pose.function.PoseFunction;
import com.trainguy9512.locomotion.animation.pose.function.SequenceEvaluatorFunction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;

public class FirstPersonMap {

    public static PoseFunction<LocalSpacePose> blendAdditiveMovementIfHoldingMap(PoseFunction<LocalSpacePose> inputPose) {
        PoseFunction<LocalSpacePose> pose = inputPose;

        for (InteractionHand interactionHand : InteractionHand.values()) {
            pose = blendAdditiveMovementIfHoldingMapInHand(pose, interactionHand);
        }

        return pose;
    }

    public static PoseFunction<LocalSpacePose> blendAdditiveMovementIfHoldingMapInHand(
            PoseFunction<LocalSpacePose> inputPose,
            InteractionHand interactionHand
    ) {
        BlendMask blendMask = BlendMask.builder()
                .defineForMultipleJoints(switch(interactionHand) {
                    case MAIN_HAND -> FirstPersonJointAnimator.RIGHT_SIDE_JOINTS;
                    case OFF_HAND -> FirstPersonJointAnimator.LEFT_SIDE_JOINTS;
                }, 1f)
                .build();

        return BlendPosesFunction.builder(inputPose)
                .addBlendInput(
                        SequenceEvaluatorFunction.builder(FirstPersonAnimationSequences.GROUND_MOVEMENT_POSE).build(),
                        context -> getMapMovementAnimationWeight(context, interactionHand),
                        blendMask)
                .build();
    }

    public static float getMapMovementAnimationWeight(PoseFunction.FunctionEvaluationState context, InteractionHand interactionHand) {
        Identifier handPose = context.driverContainer().getDriverValue(FirstPersonDrivers.getHandPoseDriver(interactionHand));
        if (handPose == FirstPersonHandPoses.MAP) {
            return 1 - LocomotionMain.CONFIG.data().firstPersonPlayer.mapMovementAnimationIntensity;
        }
        return 0;
    }

}
