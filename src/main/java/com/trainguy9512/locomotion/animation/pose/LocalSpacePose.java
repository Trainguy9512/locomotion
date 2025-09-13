package com.trainguy9512.locomotion.animation.pose;

import com.mojang.blaze3d.vertex.PoseStack;
import com.trainguy9512.locomotion.animation.joint.skeleton.BlendMask;
import com.trainguy9512.locomotion.animation.joint.skeleton.JointSkeleton;
import com.trainguy9512.locomotion.animation.joint.JointChannel;
import com.trainguy9512.locomotion.util.Interpolator;
import com.trainguy9512.locomotion.util.Transition;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public class LocalSpacePose extends Pose {

    private LocalSpacePose(JointSkeleton jointSkeleton) {
        super(jointSkeleton);
    }

    private LocalSpacePose(Pose pose) {
        super(pose);
    }


    /**
     * Creates a blank animation pose using a joint skeleton as the template.
     * @param jointSkeleton         Template joint skeleton
     * @return                      New animation pose
     */
    public static LocalSpacePose of(JointSkeleton jointSkeleton) {
        return new LocalSpacePose(jointSkeleton);
    }


    public static LocalSpacePose of(Pose pose) {
        return new LocalSpacePose(pose);
    }

    /**
     * Creates a local space pose from this component space pose.
     */
    public ComponentSpacePose convertedToComponentSpace() {
        ComponentSpacePose pose = ComponentSpacePose.of(this);
        pose.convertChildrenJointsToComponentSpace(this.getJointSkeleton().getRootJoint(), new PoseStack());
        return pose;
    }

    public LocalSpacePose mirrored() {
        LocalSpacePose mirroredPose = new LocalSpacePose(this);
        this.jointChannels.keySet().forEach(joint -> {
            JointSkeleton.JointConfiguration configuration = this.getJointSkeleton().getJointConfiguration(joint);
            String mirrorJoint = configuration.mirrorJoint() != null ? configuration.mirrorJoint() : joint;
            JointChannel mirroredTransform = this.getJointChannel(mirrorJoint).mirrored();
            mirroredPose.setJointChannel(joint, mirroredTransform);
        });
        return mirroredPose;
    }

    /**
     * Returns an animation pose interpolated between this pose and the provided pose.
     * @param other     Animation pose to interpolate to.
     * @param weight    Weight value, 0 is the original pose and 1 is the other pose.
     * @param destination       Pose to save interpolated pose onto.
     * @return          New interpolated animation pose.
     */
    public LocalSpacePose interpolated(
            LocalSpacePose other,
            float weight,
            LocalSpacePose destination
    ) {
        return this.interpolated(other, weight, null, destination);
    }

    /**
     * Returns this animation pose interpolated between this pose and the provided pose.
     * @param other             Animation pose to interpolate to.
     * @param weight            Weight value, 0 is the original pose and 1 is the other pose.
     * @param blendMask         Optional blend mask for determining which joints will interpolate.
     * @return                  New interpolated animation pose.
     */
    public LocalSpacePose interpolated(
            LocalSpacePose other,
            float weight,
            @Nullable BlendMask blendMask
    ) {
        return this.interpolated(other, weight, blendMask, this);
    }

    /**
     * Returns this animation pose interpolated between this pose and the provided pose.
     * @param other     Animation pose to interpolate to.
     * @param weight    Weight value, 0 is the original pose and 1 is the other pose.
     * @return          New interpolated animation pose.
     */
    public LocalSpacePose interpolated(
            LocalSpacePose other,
            float weight
    ) {
        return this.interpolated(other, weight, this);
    }

    /**
     * Returns an animation pose interpolated between this pose and the provided pose.
     * @param other             Animation pose to interpolate to.
     * @param weight            Weight value, 0 is the original pose and 1 is the other pose.
     * @param blendMask         Optional blend mask for determining which joints will interpolate.
     * @param destination       Pose to save interpolated pose onto.
     * @return                  New interpolated animation pose.
     */
    public LocalSpacePose interpolated(
            LocalSpacePose other,
            float weight,
            @Nullable BlendMask blendMask,
            LocalSpacePose destination
    ) {
        if (weight == 0) {
            return destination;
        }
        for (String customAttribute : this.jointSkeleton.getCustomAttributes()) {
            float attributeWeight = weight;
            if (blendMask != null) {
                attributeWeight *= blendMask.getCustomAttributeProperty(customAttribute, this.jointSkeleton);
            }
            float customAttributeA = this.customAttributes.get(customAttribute);
            float customAttributeB = other.customAttributes.get(customAttribute);
            destination.customAttributes.put(customAttribute, Interpolator.FLOAT.interpolate(customAttributeA, customAttributeB, attributeWeight));
        }
        for (String joint : this.jointSkeleton.getJoints()) {
            float jointWeight = weight;
            if (blendMask != null) {
                jointWeight *= blendMask.getJointProperty(joint, this.jointSkeleton);
            }
            if (jointWeight == 1f) {
                destination.setJointChannel(joint, other.getJointChannel(joint));
            } else {
                destination.setJointChannel(joint, destination.getJointChannel(joint).interpolate(other.getJointChannel(joint), jointWeight));
            }
        }
        return destination;
    }

    /**
     * Returns an animation pose interpolated between this pose and the provided pose using data from a transition.
     * @param other             Animation pose to interpolate to.
     * @param time              Time progress between 0 and 1
     * @param transition        Transition to use for easing and blend profile data.
     * @param blendMask         Optional blend mask for determining which joints will interpolate.
     * @param destination       Pose to save interpolated pose onto.
     * @return                  New interpolated animation pose.
     */
    public LocalSpacePose interpolatedByTransition(
            LocalSpacePose other,
            float time,
            Transition transition,
            @Nullable BlendMask blendMask,
            LocalSpacePose destination
    ) {
        if (time == 0) {
            return destination;
        }
        for (String customAttribute : this.jointSkeleton.getCustomAttributes()) {
            float attributeTime = time;
            if (transition.blendProfile() != null) {
                attributeTime /= transition.blendProfile().getCustomAttributeProperty(customAttribute, this.jointSkeleton);
                attributeTime = Mth.clamp(attributeTime, 0, 1);
            }
            attributeTime = transition.easement().ease(attributeTime);
            if (blendMask != null) {
                attributeTime *= blendMask.getCustomAttributeProperty(customAttribute, this.jointSkeleton);
            }
            float customAttributeA = this.customAttributes.get(customAttribute);
            float customAttributeB = other.customAttributes.get(customAttribute);
            destination.customAttributes.put(customAttribute, Interpolator.FLOAT.interpolate(customAttributeA, customAttributeB, attributeTime));
        }
        for (String joint : this.jointSkeleton.getJoints()) {
            float jointTime = time;
            if (transition.blendProfile() != null) {
                jointTime /= transition.blendProfile().getJointProperty(joint, this.jointSkeleton);
                jointTime = Mth.clamp(jointTime, 0, 1);
            }
            jointTime = transition.easement().ease(jointTime);
            if (blendMask != null) {
                jointTime *= blendMask.getJointProperty(joint, this.jointSkeleton);
            }
            if (jointTime == 1f) {
                destination.setJointChannel(joint, other.getJointChannel(joint));
            } else {
                destination.setJointChannel(joint, destination.getJointChannel(joint).interpolate(other.getJointChannel(joint), jointTime));
            }
        }
        return destination;
    }

    /**
     * Returns this animation pose interpolated between this pose and the provided pose using data from a transition.
     * @param other             Animation pose to interpolate to.
     * @param time              Time progress between 0 and 1
     * @param transition        Transition to use for easing and blend profile data.
     * @param blendMask         Optional blend mask for determining which joints will interpolate.
     * @return                  New interpolated animation pose.
     */
    public LocalSpacePose interpolatedByTransition(LocalSpacePose other, float time, Transition transition, @Nullable BlendMask blendMask) {
        return this.interpolatedByTransition(other, time, transition, blendMask, this);
    }

    public void multiply(LocalSpacePose other, JointChannel.TransformSpace transformSpace) {
        this.jointChannels.forEach((joint, channel) -> channel.multiply(other.jointChannels.get(joint), transformSpace, JointChannel.TransformType.ADD));
    }

    public void invert() {
        this.jointChannels.values().forEach(JointChannel::invert);
    }
}
