package com.trainguy9512.locomotion.mixin.render;

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
                    float fovScale = entityRenderDispatcher.options.fov().get() / 70f;

                    HumanoidArm fishingRodArm = getHoldingArm(player);
                    String itemJoint = fishingRodArm == HumanoidArm.LEFT ? FirstPersonJointAnimator.LEFT_ITEM_JOINT : FirstPersonJointAnimator.RIGHT_ITEM_JOINT;
                    Matrix4f itemScaleMatrix = new Matrix4f().scale(1f/16f * fovScale).scale(1, -1, -1);
                    Matrix4f itemTransform = itemScaleMatrix.mul(animationPose.getJointChannel(itemJoint).getTransform());

                    assert entityRenderDispatcher.camera != null;
                    Vector3f playerEyePosition = player.getEyePosition(partialTick).toVector3f();
                    float playerRotationX = player.getXRot(partialTick) * Mth.DEG_TO_RAD;
                    float playerRotationY = player.getYRot(partialTick) * -Mth.DEG_TO_RAD;
                    Quaternionf playerRotation = new Quaternionf().rotateY(playerRotationY).rotateX(playerRotationX);
                    Matrix4f playerTransform = new Matrix4f().translate(playerEyePosition).rotate(playerRotation);

                    Vector3f cameraPosition = entityRenderDispatcher.camera.getPosition().toVector3f();
                    Quaternionf cameraRotation = entityRenderDispatcher.camera.rotation();
                    Matrix4f cameraTransform = new Matrix4f().translate(cameraPosition);

                    Vector3f fishingLinePosition = playerTransform
                            .translate(itemTransform.getTranslation(new Vector3f()))
                            .rotate(itemTransform.getNormalizedRotation(new Quaternionf()))
                            .getTranslation(new Vector3f());
                    cir.setReturnValue(new Vec3(fishingLinePosition));

//                    entityRenderDispatcher.camera.getPosition() .getNearPlane().getPointOnPlane((float)i * 0.525F, -0.1F).scale(l);
                });
            }
        }
    }
}
