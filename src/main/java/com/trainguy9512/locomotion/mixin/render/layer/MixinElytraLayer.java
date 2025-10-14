package com.trainguy9512.locomotion.mixin.render.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.trainguy9512.locomotion.access.LivingEntityRenderStateAccess;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.layers.WingsLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(WingsLayer.class)
public abstract class MixinElytraLayer<T extends LivingEntity, S extends HumanoidRenderState, M extends EntityModel<S>> extends RenderLayer<S, M> {
    public MixinElytraLayer(RenderLayerParent<S, M> renderLayerParent) {
        super(renderLayerParent);
    }

    /*
    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/renderer/entity/state/HumanoidRenderState;FF)V", at = @At("HEAD"))
    private void transformElytra(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, S humanoidRenderState, float f, float g, CallbackInfo ci){
        if(this.getParentModel() instanceof HumanoidModel && isValidForElytraTransformation(humanoidRenderState)){
            poseStack.pushPose();
            ModelPart body = ((HumanoidModel<?>) this.getParentModel()).body;
            body.translateAndRotate(poseStack);
        }
    }

    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/renderer/entity/state/HumanoidRenderState;FF)V", at = @At("RETURN"))
    private void transformElytraFinalized(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, S humanoidRenderState, float f, float g, CallbackInfo ci){
        if(this.getParentModel() instanceof HumanoidModel && isValidForElytraTransformation(humanoidRenderState)){
            poseStack.popPose();
        }
    }
     */

    private boolean isValidForElytraTransformation(LivingEntityRenderState livingEntityRenderState){
        return ((LivingEntityRenderStateAccess)livingEntityRenderState).animationOverhaul$getInterpolatedAnimationPose() != null;
    }
}
