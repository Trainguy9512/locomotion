package com.trainguy9512.locomotion.mixin.render;

import com.trainguy9512.locomotion.access.EntityRenderStateAccess;
import com.trainguy9512.locomotion.animation.animator.JointAnimatorDispatcher;
import com.trainguy9512.locomotion.animation.data.AnimationDataContainer;
import com.trainguy9512.locomotion.animation.pose.ModelPartSpacePose;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Contract;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(EntityRenderer.class)
public class MixinEntityRenderer<T extends Entity, S extends EntityRenderState> {

    @Inject(
            method = "extractRenderState",
            at = @At("HEAD")
    )
    private void calculatePoseIntoRenderState(T entity, S reusedState, float partialTick, CallbackInfo ci) {
        Optional<AnimationDataContainer> potentialDataContainer = JointAnimatorDispatcher.getInstance().getEntityAnimationDataContainer(entity);
        if (potentialDataContainer.isPresent()) {
            AnimationDataContainer dataContainer = potentialDataContainer.get();
            ModelPartSpacePose pose = dataContainer.getInterpolatedAnimationPose(partialTick);
            ((EntityRenderStateAccess)reusedState).animationOverhaul$setInterpolatedAnimationPose(pose);
        }
    }
}
