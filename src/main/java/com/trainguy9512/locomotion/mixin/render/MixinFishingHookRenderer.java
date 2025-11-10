package com.trainguy9512.locomotion.mixin.render;

import com.mojang.blaze3d.Blaze3D;
import com.trainguy9512.locomotion.LocomotionMain;
import com.trainguy9512.locomotion.animation.animator.JointAnimatorDispatcher;
import com.trainguy9512.locomotion.animation.animator.entity.firstperson.FirstPersonJointAnimator;
import com.trainguy9512.locomotion.config.LocomotionConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.FishingHookRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FishingHookRenderer.class)
public abstract class MixinFishingHookRenderer {

    @Shadow
    public static HumanoidArm getHoldingArm(Player player) {
        return null;
    }

    @Inject(
            method = "getPlayerHandPos",
            at = @At("HEAD"),
            cancellable = true
    )
    private void injectLocomotionFishingHookPosition(Player player, float handAngle, float partialTick, CallbackInfoReturnable<Vec3> cir) {
        // Only set the hand position if the Locomotion first person player is enabled.
        if (LocomotionMain.CONFIG.data().firstPersonPlayer.enableRenderer) {
            EntityRenderDispatcher entityRenderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();

            // Only do this if the player is in first person mode.
            if (entityRenderDispatcher.options.getCameraType().isFirstPerson() && player == Minecraft.getInstance().player) {
                JointAnimatorDispatcher jointAnimatorDispatcher = JointAnimatorDispatcher.getInstance();
                jointAnimatorDispatcher.getFirstPersonPlayerDataContainer().flatMap(dataContainer -> jointAnimatorDispatcher.getInterpolatedFirstPersonPlayerPose()).ifPresent(animationPose -> {
                    float fovScale = 0;
                    fovScale = entityRenderDispatcher.options.fov().get() * (Mth.PI / (70f * 4f));
                    fovScale = (float) Math.tan(fovScale);
                    fovScale = (fovScale - 1f) * 1.2f + 1f;
//                    float fovScale = entityRenderDispatcher.options.fov().get() * Mth.DEG_TO_RAD / 2f;
//                    fovScale = (float) (-0.7f / Math.tan(fovScale)) + 2;
//                    fovScale -= 3.7032f;
//                    fovScale = 1 + 1 - fovScale;

//                    fovScale = 960f / entityRenderDispatcher.options.fov().get();

                    assert entityRenderDispatcher.camera != null;
//                    Vector3f cameraNearPlaneCenter = entityRenderDispatcher.camera.getNearPlane().getPointOnPlane(0f, 0f).toVector3f();
//                    Matrix4f cameraNearPlaneScaleMatrix = new Matrix4f().scale(fovScale).translate(cameraNearPlaneCenter);

                    HumanoidArm fishingRodArm = getHoldingArm(player);
                    String itemJoint = fishingRodArm == HumanoidArm.LEFT ? FirstPersonJointAnimator.LEFT_ITEM_JOINT : FirstPersonJointAnimator.RIGHT_ITEM_JOINT;
                    Matrix4f itemTransform = new Matrix4f()
                            .scale(1f/16f)
//                            .scale(fovScale, fovScale, 1)
                            .scale(1, -1, -1)
                            .mul(animationPose.getJointChannel(itemJoint).getTransform())
                            .translate(0, 10, 5);

                    float playerRotationX = player.getXRot(partialTick) * Mth.DEG_TO_RAD;
                    float playerRotationY = player.getYRot(partialTick) * -Mth.DEG_TO_RAD;
                    Quaternionf playerRotation = new Quaternionf().rotateY(playerRotationY).rotateX(playerRotationX);
                    Matrix4f playerTransform = new Matrix4f()
                            .translate(entityRenderDispatcher.camera.getPosition().toVector3f())
                            .rotate(playerRotation);

//                    entityRenderDispatcher.camera.getNearPlane().

                    Matrix4f cameraCenterTransform = new Matrix4f()
                            .scale(1f/16f)
                            .scale(1, -1, -1)
                            .mul(animationPose.getJointChannel(FirstPersonJointAnimator.CAMERA_JOINT).getTransform())
                            .translate(0, 0, -5);

                    Vector3f cameraPosition = entityRenderDispatcher.camera.getPosition().add(player.getEyePosition(partialTick)).toVector3f();
                    Quaternionf cameraRotation = entityRenderDispatcher.camera.rotation();
                    Matrix4f cameraTransform = new Matrix4f().translate(cameraPosition);

                    Matrix4f blendedFovItemTransform = cameraCenterTransform.lerp(itemTransform, fovScale);

                    Vector3f fishingLinePosition = playerTransform
                            .translate(blendedFovItemTransform.getTranslation(new Vector3f()))
                            .rotate(blendedFovItemTransform.getNormalizedRotation(new Quaternionf()))
                            .getTranslation(new Vector3f());

                    cir.setReturnValue(new Vec3(fishingLinePosition));

//                    entityRenderDispatcher.camera.getPosition() .getNearPlane().getPointOnPlane((float)i * 0.525F, -0.1F).scale(l);
                });
            }
        }
    }
}
