package com.trainguy9512.locomotion.mixin.render;

import com.trainguy9512.locomotion.access.EntityRenderStateAccess;
import com.trainguy9512.locomotion.animation.animator.entity.EntityJointAnimator;
import com.trainguy9512.locomotion.animation.pose.ModelPartSpacePose;
import com.trainguy9512.locomotion.animation.pose.Pose;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.Optional;

@Mixin(EntityRenderState.class)
public class MixinEntityRenderState implements EntityRenderStateAccess {

    @Unique
    private ModelPartSpacePose interpolatedPose;

    @Unique
    private EntityJointAnimator<?, ?> entityJointAnimator;

    @Unique
    @Override
    public void animationOverhaul$setInterpolatedAnimationPose(ModelPartSpacePose interpolatedPose) {
        this.interpolatedPose = interpolatedPose;
    }

    @Override
    public Optional<ModelPartSpacePose> animationOverhaul$getInterpolatedAnimationPose() {
        return Optional.ofNullable(this.interpolatedPose);
    }

    @Override
    public void animationOverhaul$setEntityJointAnimator(EntityJointAnimator<?, ?> entityJointAnimator) {
        this.entityJointAnimator = entityJointAnimator;
    }

    @Override
    public Optional<EntityJointAnimator<?, ?>> animationOverhaul$getEntityJointAnimator() {
        return Optional.ofNullable(this.entityJointAnimator);
    }
}
