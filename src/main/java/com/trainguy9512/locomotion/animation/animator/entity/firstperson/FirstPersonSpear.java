package com.trainguy9512.locomotion.animation.animator.entity.firstperson;

import com.trainguy9512.locomotion.animation.pose.LocalSpacePose;
import com.trainguy9512.locomotion.animation.pose.function.PoseFunction;
import com.trainguy9512.locomotion.animation.pose.function.cache.CachedPoseContainer;
import net.minecraft.world.InteractionHand;

public class FirstPersonSpear {

    public static PoseFunction<LocalSpacePose> constructSpearPoseFunction(CachedPoseContainer cachedPoseContainer, InteractionHand hand) {
        return FirstPersonMining.makeMainHandPickaxeMiningPoseFunction(cachedPoseContainer, hand);
    }

}
