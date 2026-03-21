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
//? if < 1.21.0 {
/*import net.minecraft.world.entity.projectile.FishingHook;*/
//?}
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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

    //? if >= 1.21.0 {
    @Inject(
            method = "getPlayerHandPos",
            at = @At("HEAD"),
            cancellable = true
    )
    private void injectLocomotionFishingHookPosition(Player player, float handAngle, float partialTick, CallbackInfoReturnable<Vec3> cir) {
        Vec3 handPos = locomotion$getFishingLinePosition(player, partialTick);
        if (handPos != null) {
            cir.setReturnValue(handPos);
        }
    }
    //?} else {
    /*@org.spongepowered.asm.mixin.injection.ModifyVariable(
            method = "render(Lnet/minecraft/world/entity/projectile/FishingHook;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(value = "STORE", ordinal = 3),
            ordinal = 0
    )
    private Vec3 locomotion$overrideHandOffset(Vec3 original, FishingHook hook, float yaw, float partialTick, com.mojang.blaze3d.vertex.PoseStack poseStack, net.minecraft.client.renderer.MultiBufferSource bufferSource, int light) {
        Player player = hook.getPlayerOwner();
        if (player == null) {
            return original;
        }
        Vec3 handPos = locomotion$getFishingLinePosition(player, partialTick);
        if (handPos == null) {
            return original;
        }
        double playerX = Mth.lerp(partialTick, player.xo, player.getX());
        double playerY = Mth.lerp(partialTick, player.yo, player.getY());
        double playerZ = Mth.lerp(partialTick, player.zo, player.getZ());
        return handPos.subtract(playerX, playerY, playerZ);
    }*/
    //?}

    private static Vec3 locomotion$getFishingLinePosition(Player player, float partialTick) {
        // Only set the hand position if the Locomotion first person player is enabled.
        if (!LocomotionMain.CONFIG.data().firstPersonPlayer.enableRenderer) {
            return null;
        }

        EntityRenderDispatcher entityRenderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        // Only do this if the player is in first person mode.
        if (!entityRenderDispatcher.options.getCameraType().isFirstPerson() || player != Minecraft.getInstance().player) {
            return null;
        }

        JointAnimatorDispatcher jointAnimatorDispatcher = JointAnimatorDispatcher.getInstance();
        return jointAnimatorDispatcher.getFirstPersonPlayerDataContainer()
                .flatMap(dataContainer -> jointAnimatorDispatcher.getInterpolatedFirstPersonPlayerPose())
                .map(animationPose -> {
                    float fovScale = 0;
                    fovScale = entityRenderDispatcher.options.fov().get() * (Mth.PI / (70f * 4f));
                    fovScale = (float) Math.tan(fovScale);


                    fovScale = entityRenderDispatcher.options.fov().get();
                    fovScale = ((1 / 70f) / 70f) * (fovScale * fovScale);
//                    float fovScale = entityRenderDispatcher.options.fov().get() * Mth.DEG_TO_RAD / 2f;
//                    fovScale = (float) (-0.7f / Math.tan(fovScale)) + 2;
//                    fovScale -= 3.7032f;
//                    fovScale = 1 + 1 - fovScale;

//                    fovScale = 960f / entityRenderDispatcher.options.fov().get();

                    assert entityRenderDispatcher.camera != null;
//                    Vector3f cameraNearPlaneCenter = entityRenderDispatcher.camera.getNearPlane().getPointOnPlane(0f, 0f).toVector3f();
//                    Matrix4f cameraNearPlaneScaleMatrix = new Matrix4f().scale(fovScale).translate(cameraNearPlaneCenter);

                    HumanoidArm fishingRodArm = locomotion$getHoldingArm(player);
                    String itemJoint = fishingRodArm == HumanoidArm.LEFT ? FirstPersonJointAnimator.LEFT_ITEM_JOINT : FirstPersonJointAnimator.RIGHT_ITEM_JOINT;
                    Matrix4f itemTransform = new Matrix4f()
                            .scale(1f/16f)
//                            .scale(fovScale, fovScale, 1)
                            .scale(1, -1, -1)
                            .mul(animationPose.getJointChannel(itemJoint).getTransform())
                            .translate(0, 10, 5);

                    //? if >= 1.21.0 {
                    float playerRotationX = player.getXRot(partialTick) * Mth.DEG_TO_RAD;
                    float playerRotationY = player.getYRot(partialTick) * -Mth.DEG_TO_RAD;
                    //?} else {
                    /*float playerRotationX = Mth.lerp(partialTick, player.xRotO, player.getXRot()) * Mth.DEG_TO_RAD;
                    float playerRotationY = Mth.lerp(partialTick, player.yRotO, player.getYRot()) * -Mth.DEG_TO_RAD;*/
                    //?}
                    Quaternionf playerRotation = new Quaternionf().rotateY(playerRotationY).rotateX(playerRotationX);
                    Matrix4f playerTransform = new Matrix4f()
                            //? if >= 1.21.0 {
                            .translate(entityRenderDispatcher.camera.position().toVector3f())
                            //?} else {
                            /*.translate(entityRenderDispatcher.camera.getPosition().toVector3f())*/
                            //?}
                            .rotate(playerRotation);

//                    entityRenderDispatcher.camera.getNearPlane().

                    Matrix4f cameraCenterTransform = new Matrix4f()
                            .scale(1f/16f)
                            .scale(1, -1, -1)
                            .mul(animationPose.getJointChannel(FirstPersonJointAnimator.CAMERA_JOINT).getTransform())
                            .translate(0, 0, -5);

                    //? if >= 1.21.0 {
                    Vector3f cameraPosition = entityRenderDispatcher.camera.position().add(player.getEyePosition(partialTick)).toVector3f();
                    //?} else {
                    /*Vector3f cameraPosition = entityRenderDispatcher.camera.getPosition().add(player.getEyePosition(partialTick)).toVector3f();*/
                    //?}
                    Quaternionf cameraRotation = entityRenderDispatcher.camera.rotation();
                    Matrix4f cameraTransform = new Matrix4f().translate(cameraPosition);

                    Matrix4f blendedFovItemTransform = cameraCenterTransform.lerp(itemTransform, fovScale);

                    Vector3f fishingLinePosition = playerTransform
                            .translate(blendedFovItemTransform.getTranslation(new Vector3f()))
                            .rotate(blendedFovItemTransform.getNormalizedRotation(new Quaternionf()))
                            .getTranslation(new Vector3f());

                    return new Vec3(fishingLinePosition);
                })
                .orElse(null);
    }

    private static HumanoidArm locomotion$getHoldingArm(Player player) {
        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();
        HumanoidArm mainArm = player.getMainArm();
        if (mainHand.is(Items.FISHING_ROD)) {
            return mainArm;
        }
        if (offHand.is(Items.FISHING_ROD)) {
            return mainArm.getOpposite();
        }
        return mainArm;
    }
}
