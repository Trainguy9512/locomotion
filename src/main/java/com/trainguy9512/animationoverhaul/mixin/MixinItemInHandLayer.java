package com.trainguy9512.animationoverhaul.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.trainguy9512.animationoverhaul.animation.AnimatorDispatcher;
import com.trainguy9512.animationoverhaul.animation.entity.FirstPersonPlayerAnimator;
import com.trainguy9512.animationoverhaul.animation.entity.PlayerPartAnimator;
import com.trainguy9512.animationoverhaul.animation.pose.BakedAnimationPose;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(ItemInHandLayer.class)
public abstract class MixinItemInHandLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {

    public MixinItemInHandLayer(RenderLayerParent<T, M> renderLayerParent) {
        super(renderLayerParent);
    }

    @Inject(method = "renderArmWithItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"))
    private void transformItemInHandLayer(LivingEntity livingEntity, ItemStack itemStack, ItemDisplayContext itemDisplayContext, HumanoidArm humanoidArm, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, CallbackInfo ci){
        if(shouldTransformItemInHand(livingEntity)){


            //TODO: Redo how hand stuff works, add override functions to living entity animators.
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
        }
    }
    private boolean shouldTransformItemInHand(LivingEntity livingEntity){
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
