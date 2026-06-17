package com.trainguy9512.locomotion.animation.animator.entity;

import com.trainguy9512.locomotion.LocomotionMain;
import com.trainguy9512.locomotion.animation.data.DriverGetter;
import com.trainguy9512.locomotion.animation.pose.LocalSpacePose;
import com.trainguy9512.locomotion.animation.pose.function.CustomAttributeModifierFunction;
import com.trainguy9512.locomotion.animation.pose.function.EmptyPoseFunction;
import com.trainguy9512.locomotion.animation.pose.function.JointTransformerFunction;
import com.trainguy9512.locomotion.animation.pose.function.PoseFunction;
import com.trainguy9512.locomotion.animation.pose.function.cache.CachedPoseContainer;
import com.trainguy9512.locomotion.animation.pose.function.montage.MontageManager;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

public class ThirdPersonPlayerJointAnimator implements EntityJointAnimator<Player, AvatarRenderState> {
    @Override
    public void postProcessModelParts(EntityModel<AvatarRenderState> entityModel, AvatarRenderState entityRenderState) {

    }

    @Override
    public Identifier getJointSkeleton() {
        return Identifier.fromNamespaceAndPath(LocomotionMain.MOD_ID, "skeletons/entity/player/third_person.json");
    }

    @Override
    public void extractAnimationData(Player dataReference, DriverGetter dataContainer, MontageManager montageManager) {
        dataContainer.getDriver(LivingEntityJointAnimator.ENTITY_ROTATION).setValue(dataReference.getYRot());
    }

    @Override
    public PoseCalculationFrequency getPoseCalulationFrequency() {
        return PoseCalculationFrequency.CALCULATE_EVERY_FRAME;
    }

    @Override
    public PoseFunction<LocalSpacePose> constructPoseFunction(CachedPoseContainer cachedPoseContainer) {
        PoseFunction<LocalSpacePose> pose = EmptyPoseFunction.of(true);
        pose = CustomAttributeModifierFunction.of(
                pose,
                LivingEntityJointAnimator.ENTITY_ROTATION_ATTRIBUTE,
                context -> context.getDriverValue(LivingEntityJointAnimator.ENTITY_ROTATION)
        );

        return pose;
    }
}
