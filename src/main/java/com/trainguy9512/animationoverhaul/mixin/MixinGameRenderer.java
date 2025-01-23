package com.trainguy9512.animationoverhaul.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.trainguy9512.animationoverhaul.AnimationOverhaulMain;
import com.trainguy9512.animationoverhaul.animation.EntityJointAnimatorDispatcher;
import com.trainguy9512.animationoverhaul.animation.animator.FirstPersonPlayerJointAnimator;
import com.trainguy9512.animationoverhaul.animation.pose.AnimationPose;
import com.trainguy9512.animationoverhaul.animation.pose.JointTransform;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class MixinGameRenderer {
    @Shadow @Final Minecraft minecraft;



    /*
    @Inject(method = "renderLevel", at = @At("HEAD"))
    private void adjustTimersForAllEntities(float f, long l, PoseStack poseStack, CallbackInfo ci){
        for(Entity entity : this.minecraft.level.entitiesForRendering()){
            if(entity instanceof LivingEntity){

                EntityType<?> entityType = entity.getType();
                if(AnimationOverhaulMain.ENTITY_ANIMATORS.contains(entityType)){
                    LivingEntityAnimator livingEntityAnimator = AnimationOverhaulMain.ENTITY_ANIMATORS.get(entityType);
                    livingEntityAnimator.setPartialTicks(f);
                    livingEntityAnimator.tick((LivingEntity) entity);
                }
            }
        }
    }

     */


    @Shadow private boolean renderHand;

    @Shadow @Final private Camera mainCamera;

    @Inject(method = "tick", at = @At("TAIL"))
    private <T extends Entity, L extends Enum<L>> void tickEntityInformation(CallbackInfo ci){
        if(this.minecraft.level != null){
            for(Entity entity : this.minecraft.level.entitiesForRendering()){
                if(entity instanceof LivingEntity){
                    EntityType<?> entityType = entity.getType();
                    if(AnimationOverhaulMain.ENTITY_ANIMATORS.contains(entityType)){
                        EntityJointAnimatorDispatcher.INSTANCE.tick(entity);
                    }
                }
            }
            // Special functionality for the first person player joint animator
            FirstPersonPlayerJointAnimator.INSTANCE.tickExternal();
        }

    }

    @Inject(method = "bobView", at = @At(value = "HEAD"), cancellable = true)
    private void injectCameraRotation(PoseStack poseStack, float f, CallbackInfo ci){
        if(this.minecraft.options.getCameraType().isFirstPerson() && this.renderHand){
            if(FirstPersonPlayerJointAnimator.INSTANCE.localBakedPose != null){
                AnimationPose<FirstPersonPlayerJointAnimator.FPPlayerJoints> animationPose = FirstPersonPlayerJointAnimator.INSTANCE.localBakedPose.getBlendedPose(f);
                JointTransform cameraPose = animationPose.getJointTransform(FirstPersonPlayerJointAnimator.FPPlayerJoints.camera);
                JointTransform rootPose = animationPose.getJointTransform(FirstPersonPlayerJointAnimator.FPPlayerJoints.root);
                cameraPose.multiplyTransform(rootPose);

                //poseStack.translate(cameraPose.y / 16F, cameraPose.x / -16F, cameraPose.z / -16F);

                PoseStack poseStack1 = new PoseStack();
                Vector3f cameraRot = cameraPose.getEulerRotationZYX();
                cameraRot.z *= -1;
                cameraPose.setRotation(cameraRot);

                poseStack1.mulPose(cameraPose.getRotation());
                poseStack1.translate(cameraPose.getTranslation().x / 16F, cameraPose.getTranslation().y / 16F, cameraPose.getTranslation().z / -16F);
                Matrix4f matrix4f = poseStack1.last().pose();

                poseStack.mulPose(matrix4f);
                //poseStack.mulPose(new Quaternionf().rotationZYX(-cameraPose.zRot, cameraPose.yRot, cameraPose.xRot));

                //poseStack.mulPose(Axis.XP.rotationDegrees(this.mainCamera.getXRot()));
                //poseStack.mulPose(Axis.YP.rotationDegrees(this.mainCamera.getYRot() + 180.0f));

                //cameraPose.transformPoseStack(poseStack);
            }
        } else {
            poseStack.mulPose(Axis.XP.rotationDegrees(this.mainCamera.getXRot()));
            poseStack.mulPose(Axis.YP.rotationDegrees(this.mainCamera.getYRot() + 180.0f));
        }
    }

    @Redirect(method = "renderItemInHand", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;bobView(Lcom/mojang/blaze3d/vertex/PoseStack;F)V"))
    private void removeHandBobbing(GameRenderer instance, PoseStack poseStack, float f){

    }
}
