package com.trainguy9512.locomotion.animation.animator.block_entity;

import com.trainguy9512.locomotion.LocomotionMain;
import com.trainguy9512.locomotion.animation.animator.JointAnimator;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface BlockEntityJointAnimator<B extends BlockEntity> extends JointAnimator<B> {

    @Override
    default PoseCalculationFrequency getPoseCalulationFrequency() {
        return LocomotionMain.CONFIG.data().blockEntities.poseCalculationFrequency;
    }
}
