package com.trainguy9512.locomotion.animation.animator.block_entity;

import com.trainguy9512.locomotion.LocomotionMain;
import com.trainguy9512.locomotion.animation.data.OnTickDriverContainer;
import com.trainguy9512.locomotion.animation.driver.DriverKey;
import com.trainguy9512.locomotion.animation.driver.VariableDriver;
import com.trainguy9512.locomotion.animation.pose.LocalSpacePose;
import com.trainguy9512.locomotion.animation.pose.function.PoseFunction;
import com.trainguy9512.locomotion.animation.pose.function.SequenceEvaluatorFunction;
import com.trainguy9512.locomotion.animation.pose.function.SequencePlayerFunction;
import com.trainguy9512.locomotion.animation.pose.function.cache.CachedPoseContainer;
import com.trainguy9512.locomotion.animation.pose.function.montage.MontageManager;
import com.trainguy9512.locomotion.animation.pose.function.statemachine.StateDefinition;
import com.trainguy9512.locomotion.animation.pose.function.statemachine.StateMachineFunction;
import com.trainguy9512.locomotion.animation.pose.function.statemachine.StateTransition;
import com.trainguy9512.locomotion.animation.util.TimeSpan;
import com.trainguy9512.locomotion.animation.util.Transition;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.LidBlockEntity;

public class ChestJointAnimator<B extends BlockEntity & LidBlockEntity> implements TwoStateContainerJointAnimator<B> {

    public static final Identifier CHEST_SKELETON = LocomotionMain.makeIdentifier("skeletons/block_entity/chest.json");

    public static final Identifier CHEST_OPEN_SEQUENCE = LocomotionMain.makeIdentifier("sequences/block_entity/chest/open.json");
    public static final Identifier CHEST_CLOSE_SEQUENCE = LocomotionMain.makeIdentifier("sequences/block_entity/chest/close.json");

    @Override
    public Identifier getJointSkeleton() {
        return CHEST_SKELETON;
    }

    @Override
    public float getOpenProgress(B blockEntity) {
        return blockEntity.getOpenNess(0);
    }

    @Override
    public Identifier getOpenAnimationSequence() {
        return CHEST_OPEN_SEQUENCE;
    }

    @Override
    public Identifier getCloseAnimationSequence() {
        return CHEST_CLOSE_SEQUENCE;
    }
}
