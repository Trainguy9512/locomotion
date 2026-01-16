package com.trainguy9512.locomotion.animation.animator.entity.firstperson;

import com.trainguy9512.locomotion.LocomotionMain;
import com.trainguy9512.locomotion.animation.joint.JointChannel;
import com.trainguy9512.locomotion.animation.pose.LocalSpacePose;
import com.trainguy9512.locomotion.animation.pose.function.JointTransformerFunction;
import com.trainguy9512.locomotion.animation.pose.function.PoseFunction;
import org.joml.Vector3f;

public class FirstPersonArmOffset {

    public static PoseFunction<LocalSpacePose> constructWithArmXOffset(PoseFunction<LocalSpacePose> inputPose) {
        PoseFunction<LocalSpacePose> pose = inputPose;

        pose = JointTransformerFunction.localOrParentSpaceBuilder(pose, FirstPersonJointAnimator.RIGHT_ARM_BUFFER_JOINT)
                .setTranslation(
                        context -> new Vector3f(LocomotionMain.CONFIG.data().firstPersonPlayer.armOffsetX, 0, 0),
                        JointChannel.TransformType.ADD,
                        JointChannel.TransformSpace.PARENT
                ).build();

        pose = JointTransformerFunction.localOrParentSpaceBuilder(pose, FirstPersonJointAnimator.LEFT_ARM_BUFFER_JOINT)
                .setTranslation(
                        context -> new Vector3f(-LocomotionMain.CONFIG.data().firstPersonPlayer.armOffsetX, 0, 0),
                        JointChannel.TransformType.ADD,
                        JointChannel.TransformSpace.PARENT
                ).build();

        return pose;
    }

    public static PoseFunction<LocalSpacePose> constructWithArmYZOffset(PoseFunction<LocalSpacePose> inputPose) {
        PoseFunction<LocalSpacePose> pose = inputPose;

        pose = JointTransformerFunction.localOrParentSpaceBuilder(pose, FirstPersonJointAnimator.ARM_BUFFER_JOINT)
                .setTranslation(
                        context -> new Vector3f(
                                0,
                                -LocomotionMain.CONFIG.data().firstPersonPlayer.armOffsetY,
                                LocomotionMain.CONFIG.data().firstPersonPlayer.armOffsetZ
                        ),
                        JointChannel.TransformType.ADD,
                        JointChannel.TransformSpace.PARENT
                ).build();

        return pose;
    }

}
