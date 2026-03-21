package com.trainguy9512.locomotion.mixin.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.trainguy9512.locomotion.render.FirstPersonPlayerRenderer;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemTransform.class)
public class MixinItemTransform {

    //? if >= 1.21.0 {
    @Inject(
            method = "apply",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack$Pose;scale(FFF)V")
    )
    public void flipItemModel(boolean bl, PoseStack.Pose pose, CallbackInfo ci) {
        if (FirstPersonPlayerRenderer.SHOULD_FLIP_ITEM_TRANSFORM && FirstPersonPlayerRenderer.IS_RENDERING_LOCOMOTION_FIRST_PERSON) {
            //? if >= 1.21.0 {
            pose.rotate(Axis.YP.rotation(Mth.PI));
            //?} else {
            /*pose.pose().rotate(Axis.YP.rotation(Mth.PI));*/
            //?}
        }
    }
    //?} else {
    /*@Inject(
            method = "apply",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;scale(FFF)V")
    )
    public void flipItemModel(boolean bl, PoseStack poseStack, CallbackInfo ci) {
        if (FirstPersonPlayerRenderer.SHOULD_FLIP_ITEM_TRANSFORM && FirstPersonPlayerRenderer.IS_RENDERING_LOCOMOTION_FIRST_PERSON) {
            poseStack.mulPose(Axis.YP.rotation(Mth.PI));
        }
    }*/
    //?}
}
