package com.trainguy9512.locomotion.animation.animator.block_entity;

import com.trainguy9512.locomotion.LocomotionMain;
import com.trainguy9512.locomotion.animation.data.OnTickDriverContainer;
import com.trainguy9512.locomotion.animation.pose.LocalSpacePose;
import com.trainguy9512.locomotion.animation.pose.function.PoseFunction;
import com.trainguy9512.locomotion.animation.pose.function.SequencePlayerFunction;
import com.trainguy9512.locomotion.animation.pose.function.cache.CachedPoseContainer;
import com.trainguy9512.locomotion.animation.pose.function.montage.MontageManager;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;

public class ShulkerBoxJointAnimator implements BlockEntityJointAnimator<@org.jetbrains.annotations.NotNull ShulkerBoxBlockEntity> {

    public static final Identifier SHULKER_BOX_SKELETON = LocomotionMain.makeIdentifier("skeletons/block_entity/shulker_box.json");

    public static final Identifier SHULKER_BOX_OPEN_SEQUENCE = LocomotionMain.makeIdentifier("sequences/block_entity/shulker_box/open.json");

    @Override
    public Identifier getJointSkeleton() {
        return SHULKER_BOX_SKELETON;
    }

    @Override
    public void extractAnimationData(ShulkerBoxBlockEntity dataReference, OnTickDriverContainer dataContainer, MontageManager montageManager) {

    }

    @Override
    public PoseFunction<LocalSpacePose> constructPoseFunction(CachedPoseContainer cachedPoseContainer) {
        return SequencePlayerFunction.builder(SHULKER_BOX_OPEN_SEQUENCE).setLooping(true).build();
    }
}
