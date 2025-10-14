package com.trainguy9512.locomotion.mixin.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.trainguy9512.locomotion.LocomotionMain;
import com.trainguy9512.locomotion.access.FirstPersonPlayerRendererGetter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public class MixinItemInHandRenderer {

    @Shadow @Final private Minecraft minecraft;

    @Inject(
            method = "renderHandsWithItems",
            at = @At("HEAD"),
            cancellable = true
    )
    public void overrideFirstPersonRendering(
            float partialTicks, PoseStack poseStack, SubmitNodeCollector nodeCollector, LocalPlayer player, int packedLight, CallbackInfo ci
    ) {
        if (LocomotionMain.CONFIG.data().firstPersonPlayer.enableRenderer) {
            ((FirstPersonPlayerRendererGetter) this.minecraft.getEntityRenderDispatcher()).locomotion$getFirstPersonPlayerRenderer().ifPresent(firstPersonPlayerRenderer -> firstPersonPlayerRenderer.render(partialTicks, poseStack, nodeCollector, player, packedLight));
            ci.cancel();
        }
    }
}
