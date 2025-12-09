package com.trainguy9512.locomotion.mixin.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.trainguy9512.locomotion.LocomotionMain;
import com.trainguy9512.locomotion.access.FirstPersonPlayerRendererGetter;
import com.trainguy9512.locomotion.animation.animator.JointAnimatorDispatcher;
import com.trainguy9512.locomotion.animation.animator.JointAnimatorRegistry;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class MixinGameRenderer {

    @Shadow @Final private Minecraft minecraft;

    /**
     * Computes and saves the interpolated animation pose prior to rendering.
     */
    @Inject(
            method = "renderLevel",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;extractCamera(F)V")
    )
    private void computePosePriorToRendering(DeltaTracker deltaTracker, CallbackInfo ci){
        if (LocomotionMain.CONFIG.data().firstPersonPlayer.enableRenderer) {
            JointAnimatorDispatcher jointAnimatorDispatcher = JointAnimatorDispatcher.getInstance();
            jointAnimatorDispatcher.getFirstPersonPlayerDataContainer().ifPresent(dataContainer ->
                    JointAnimatorRegistry.getFirstPersonPlayerJointAnimator().ifPresent(
                            jointAnimator -> jointAnimatorDispatcher.calculateInterpolatedFirstPersonPlayerPose(jointAnimator, dataContainer, deltaTracker.getGameTimeDeltaPartialTick(true))

                    ));
        }
    }

    /**
     * Transform the camera pose stack based on the first person player's camera joint, prior to bobHurt and bobView.
     */
    @Inject(
            method = "bobHurt",
            at = @At("HEAD")
    )
    private void addCameraRotation(PoseStack poseStack, float partialTicks, CallbackInfo ci){
        if (LocomotionMain.CONFIG.data().firstPersonPlayer.enableRenderer) {
            ((FirstPersonPlayerRendererGetter)this.minecraft.getEntityRenderDispatcher()).locomotion$getFirstPersonPlayerRenderer().ifPresent(firstPersonPlayerRenderer -> firstPersonPlayerRenderer.transformCamera(poseStack));
        }

    }

    /**
     * Remove the view bobbing animation, as the animation pose provides its own.
     * When config is added to enable/disable custom first person rendering, this should be revisited!
     */
    @Inject(
            method = "bobView",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    private void removeViewBobbing(PoseStack poseStack, float partialTicks, CallbackInfo ci){
        if (LocomotionMain.CONFIG.data().firstPersonPlayer.enableRenderer) {
            ci.cancel();
        }
    }
}
