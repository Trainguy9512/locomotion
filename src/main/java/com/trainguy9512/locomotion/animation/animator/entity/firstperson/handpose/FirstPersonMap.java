package com.trainguy9512.locomotion.animation.animator.entity.firstperson.handpose;

import com.trainguy9512.locomotion.LocomotionMain;
import com.trainguy9512.locomotion.animation.animator.entity.firstperson.FirstPersonAnimationSequences;
import com.trainguy9512.locomotion.animation.animator.entity.firstperson.FirstPersonDrivers;
import com.trainguy9512.locomotion.animation.animator.entity.firstperson.FirstPersonJointAnimator;
import com.trainguy9512.locomotion.animation.data.PoseTickEvaluationContext;
import com.trainguy9512.locomotion.animation.joint.skeleton.BlendMask;
import com.trainguy9512.locomotion.animation.pose.LocalSpacePose;
import com.trainguy9512.locomotion.animation.pose.function.BlendPosesFunction;
import com.trainguy9512.locomotion.animation.pose.function.PoseFunction;
import com.trainguy9512.locomotion.animation.pose.function.SequenceEvaluatorFunction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;

public class FirstPersonMap {

    public static PoseFunction<LocalSpacePose> blendAdditiveMovementIfHoldingMap(PoseFunction<LocalSpacePose> inputPose) {
        PoseFunction<LocalSpacePose> pose = inputPose;

        for (InteractionHand hand : InteractionHand.values()) {
            pose = blendAdditiveMovementIfHoldingMapInHand(pose, hand);
        }

        return pose;
    }

    public static PoseFunction<LocalSpacePose> blendAdditiveMovementIfHoldingMapInHand(
            PoseFunction<LocalSpacePose> inputPose,
            InteractionHand hand
    ) {
        BlendMask blendMask = BlendMask.builder()
                .defineForMultipleJoints(switch(hand) {
                    case MAIN_HAND -> FirstPersonJointAnimator.RIGHT_SIDE_JOINTS;
                    case OFF_HAND -> FirstPersonJointAnimator.LEFT_SIDE_JOINTS;
                }, 1f)
                .build();

        return BlendPosesFunction.builder(inputPose)
                .addBlendInput(
                        SequenceEvaluatorFunction.builder(FirstPersonAnimationSequences.GROUND_MOVEMENT_POSE).build(),
                        context -> getMapMovementAnimationWeight(context, hand),
                        blendMask)
                .build();
    }

    public static float getMapMovementAnimationWeight(PoseTickEvaluationContext context, InteractionHand hand) {
        Identifier handPose = context.driverContainer().getDriverValue(FirstPersonDrivers.getHandPoseDriver(hand));
        if (handPose == FirstPersonHandPoses.MAP) {
            return 1 - LocomotionMain.CONFIG.data().firstPersonPlayer.mapMovementAnimationIntensity;
        }
        return 0;
    }

}
