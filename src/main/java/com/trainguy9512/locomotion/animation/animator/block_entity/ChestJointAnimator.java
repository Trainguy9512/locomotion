package com.trainguy9512.locomotion.animation.animator.block_entity;

import com.trainguy9512.locomotion.LocomotionMain;
import com.trainguy9512.locomotion.animation.data.OnTickDriverContainer;
import com.trainguy9512.locomotion.animation.pose.LocalSpacePose;
import com.trainguy9512.locomotion.animation.pose.function.PoseFunction;
import com.trainguy9512.locomotion.animation.pose.function.SequencePlayerFunction;
import com.trainguy9512.locomotion.animation.pose.function.cache.CachedPoseContainer;
import com.trainguy9512.locomotion.animation.pose.function.montage.MontageManager;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.ChestBlockEntity;

public class ChestJointAnimator implements BlockEntityJointAnimator<@org.jetbrains.annotations.NotNull ChestBlockEntity> {

    public static final Identifier CHEST_SKELETON = LocomotionMain.makeIdentifier("skeletons/block_entity/chest.json");

    public static final Identifier OPEN_SEQUENCE = LocomotionMain.makeIdentifier("sequences/block_entity/chest/open.json");

    @Override
    public Identifier getJointSkeleton() {
        return CHEST_SKELETON;
    }

    @Override
    public void extractAnimationData(ChestBlockEntity dataReference, OnTickDriverContainer dataContainer, MontageManager montageManager) {

    }

    @Override
    public PoseFunction<LocalSpacePose> constructPoseFunction(CachedPoseContainer cachedPoseContainer) {
        return SequencePlayerFunction.builder(OPEN_SEQUENCE).setLooping(true).build();
    }

    @Override
    public PoseCalculationFrequency getPoseCalulationFrequency() {
        return PoseCalculationFrequency.CALCULATE_EVERY_FRAME;
    }
}
