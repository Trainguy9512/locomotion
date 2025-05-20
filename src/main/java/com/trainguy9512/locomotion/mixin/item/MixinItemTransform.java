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

    @Inject(
            method = "apply",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack$Pose;scale(FFF)V")
    )
    public void flipItemModel(boolean bl, PoseStack.Pose pose, CallbackInfo ci) {
        if (FirstPersonPlayerRenderer.SHOULD_FLIP_ITEM_TRANSFORM) {
            pose.rotate(Axis.YP.rotation(Mth.PI));
        }
    }
}
