package com.trainguy9512.locomotion.mixin.render;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.trainguy9512.locomotion.LocomotionMain;
import com.trainguy9512.locomotion.access.FirstPersonPlayerRendererGetter;
import com.trainguy9512.locomotion.render.FirstPersonPlayerRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.joml.Quaternionfc;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(ItemInHandRenderer.class)
public class MixinItemInHandRenderer {

    @Shadow @Final private Minecraft minecraft;

    @Inject(
            method = "renderHandsWithItems",
            at = @At("HEAD")
    )
    public void locomotion$overrideFirstPersonRendering(
            float partialTick, PoseStack poseStack, SubmitNodeCollector nodeCollector, LocalPlayer player, int packedLight, CallbackInfo ci
    ) {
        if (!LocomotionMain.CONFIG.data().firstPersonPlayer.enableRenderer) {
            return;
        }
        if (((FirstPersonPlayerRendererGetter) this.minecraft.getEntityRenderDispatcher()).locomotion$getFirstPersonPlayerRenderer().isEmpty()) {
            return;
        }
        FirstPersonPlayerRenderer renderer = ((FirstPersonPlayerRendererGetter) this.minecraft.getEntityRenderDispatcher()).locomotion$getFirstPersonPlayerRenderer().get();
        renderer.renderLocomotionArmWithItem(partialTick, poseStack, nodeCollector, player, packedLight, InteractionHand.OFF_HAND);
        renderer.renderLocomotionArmWithItem(partialTick, poseStack, nodeCollector, player, packedLight, InteractionHand.MAIN_HAND);
    }

    // Disables camera bob rotation if Locomotion's first person animations are enabled.
    @Redirect(
            method = "renderHandsWithItems",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;mulPose(Lorg/joml/Quaternionfc;)V")
    )
    public void removeVanillaCameraBob(PoseStack instance, Quaternionfc pose) {
        if (!LocomotionMain.CONFIG.data().firstPersonPlayer.enableRenderer) {
            instance.mulPose(pose);
        }
    }

    // Cancel rendering the vanilla hand with item if Locomotion's first person animations are enabled.
    @Inject(
            method = "renderArmWithItem",
            at = @At("HEAD"),
            cancellable = true
    )
    public void locomotion$renderLocomotionArmsWithItems(
            AbstractClientPlayer player, float partialTick, float pitch, InteractionHand hand, float swingProgress, ItemStack item, float equippedProgress, PoseStack poseStack, SubmitNodeCollector nodeCollector, int packedLight, CallbackInfo ci
    ) {
        if (LocomotionMain.CONFIG.data().firstPersonPlayer.enableRenderer) {
            ci.cancel();
        }
    }
}
