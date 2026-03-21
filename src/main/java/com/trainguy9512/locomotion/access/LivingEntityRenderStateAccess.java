package com.trainguy9512.locomotion.access;

import com.trainguy9512.locomotion.animation.animator.entity.EntityJointAnimator;
import com.trainguy9512.locomotion.animation.pose.Pose;

import java.util.Optional;

public interface LivingEntityRenderStateAccess {
    void animationOverhaul$setInterpolatedAnimationPose(Pose interpolatedPose);
    Optional<Pose> animationOverhaul$getInterpolatedAnimationPose();

    //? if >= 1.21.9 {
    void animationOverhaul$setEntityJointAnimator(EntityJointAnimator<?, ?> livingEntityJointAnimator);
    Optional<EntityJointAnimator<?, ?>> animationOverhaul$getEntityJointAnimator();
    //?} else {
    /*void animationOverhaul$setEntityJointAnimator(EntityJointAnimator<?> livingEntityJointAnimator);
    Optional<EntityJointAnimator<?>> animationOverhaul$getEntityJointAnimator();*/
    //?}
}
