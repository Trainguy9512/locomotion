package com.trainguy9512.locomotion.mixin.item;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(ItemInHandLayer.class)
public abstract class MixinItemInHandLayer<T extends LivingEntity, S extends ArmedEntityRenderState, M extends EntityModel<S>> extends RenderLayer<S, M> {

    public MixinItemInHandLayer(RenderLayerParent<S, M> renderLayerParent) {
        super(renderLayerParent);
    }

//    @Inject(method = "renderArmWithItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/item/ItemStackRenderState;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V"))
//    private void transformItemInHandLayer(S armedEntityRenderState, ItemStackRenderState itemStackRenderState, HumanoidArm humanoidArm, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, CallbackInfo ci){
//        if(shouldTransformItemInHand(armedEntityRenderState)){


            //TODO: Redo how hand stuff works, add override functions to living entity animators. (2025 update: what does this mean?)
            //update: gosh darnit 1.20 broke something else with this mixin
            /*
            poseStack.popPose();
            poseStack.pushPose();
            ((ArmedModel)this.getParentModel()).translateToHand(humanoidArm, poseStack);
            poseStack.translate((humanoidArm == HumanoidArm.LEFT ? 1 : -1) /16F, 8/16F, 0);

            Enum<> locatorIdentifier = humanoidArm == HumanoidArm.LEFT ? PlayerPartAnimator.ModelPartLocators.leftHand : PlayerPartAnimator.ModelPartLocators.rightHand;
            //AnimatorDispatcher.INSTANCE.getBakedPose(livingEntity.getUUID()).getLocator(locatorIdentifier, Minecraft.getInstance().getFrameTime()).translateAndRotatePoseStack(poseStack);

            BakedAnimationPose<L> bakedAnimationPose = AnimatorDispatcher.INSTANCE.getBakedPose(livingEntity.getUUID());
            bakedAnimationPose.getBlendedPose(Minecraft.getInstance().getFrameTime()).getLocatorPose(locatorIdentifier).translateAndRotatePoseStack(poseStack);

        poseStack.mulPose(Axis.XP.rotationDegrees(-90));
            poseStack.mulPose(Axis.YP.rotationDegrees(180));

            //poseStack.mulPose(Vector3f.XP.rotationDegrees(Util.getMillis() / 10F));
            poseStack.translate(0, 2/16F, -2/16F);

             */
//        }
//    }
    private boolean shouldTransformItemInHand(LivingEntityRenderState livingEntityRenderState){
        return false;
        /*
        BakedAnimationPose bakedPose = AnimatorDispatcher.INSTANCE.getBakedPose(livingEntity.getUUID());
        if(bakedPose != null){
            if(bakedPose.containsLocator("leftHand") && bakedPose.containsLocator("rightHand")){
                return true;
            }
        }
        return false;

         */
    }
}
