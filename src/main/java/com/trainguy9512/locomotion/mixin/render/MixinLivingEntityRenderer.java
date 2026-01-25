package com.trainguy9512.locomotion.mixin.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.trainguy9512.locomotion.access.LivingEntityRenderStateAccess;
import com.trainguy9512.locomotion.animation.animator.JointAnimatorRegistry;
import com.trainguy9512.locomotion.animation.animator.JointAnimatorDispatcher;
import com.trainguy9512.locomotion.animation.animator.entity.EntityJointAnimator;
import com.trainguy9512.locomotion.animation.pose.Pose;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(LivingEntityRenderer.class)
public abstract class MixinLivingEntityRenderer<S extends EntityRenderState, R extends LivingEntityRenderState, T extends LivingEntity, M extends EntityModel<S>> extends EntityRenderer<T, S> implements RenderLayerParent<S, M> {
    protected MixinLivingEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Shadow protected M model;
    @Shadow public abstract @NotNull M getModel();

    //@Shadow protected abstract void setupRotations(T livingEntity, PoseStack poseStack, float f, float g, float h);


    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V", at = @At("HEAD"))
    private <L extends Enum<L>> void extractAnimationPoseToRenderState(T livingEntity, R livingEntityRenderState, float partialTicks, CallbackInfo ci){
//        JointAnimatorDispatcher entityJointAnimatorDispatcher = JointAnimatorDispatcher.getInstance();
//        JointAnimatorRegistry.getThirdPersonJointAnimator(livingEntity).ifPresent(jointAnimator ->
//                entityJointAnimatorDispatcher.getEntityAnimationDataContainer(livingEntity).ifPresent(dataContainer -> {
//                    ((LivingEntityRenderStateAccess) livingEntityRenderState).animationOverhaul$setInterpolatedAnimationPose(entityJointAnimatorDispatcher.getInterpolatedAnimationPose(jointAnimator, dataContainer, partialTicks));
//                    ((LivingEntityRenderStateAccess) livingEntityRenderState).animationOverhaul$setEntityJointAnimator(jointAnimator);
//                })
//        );
    }

    /*
    @SuppressWarnings("unchecked")
    @Redirect(method = "render(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/EntityModel;setupAnim(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;)V"))
    private void redirectSetupAnim(EntityModel<S> entityModel, S livingEntityRenderState){
        // Unchecked cast, but I can make assumptions given this is always called after extractRenderState within the same renderer class.
        Optional<Pose> animationPoseOptional = ((LivingEntityRenderStateAccess)livingEntityRenderState).animationOverhaul$getInterpolatedAnimationPose();
        Optional<EntityJointAnimator<?, ?>> entityJointAnimatorOptional = ((LivingEntityRenderStateAccess)livingEntityRenderState).animationOverhaul$getEntityJointAnimator();

        animationPoseOptional.ifPresentOrElse(
                animationPose -> entityJointAnimatorOptional.ifPresent(jointAnimator -> {
                    EntityJointAnimator<?, S> entityJointAnimator = (EntityJointAnimator<?, S>) jointAnimator;
                    JointAnimatorDispatcher.getInstance().setupAnimWithAnimationPose(entityModel, livingEntityRenderState, animationPose, entityJointAnimator);
                }),
                () -> entityModel.setupAnim(livingEntityRenderState)
        );
    }










    @Inject(method = "setupRotations", at = @At("HEAD"), cancellable = true)
    private void overrideSetupRotation(R livingEntityRenderState, PoseStack poseStack, float f, float g, CallbackInfo ci){
        //AnimationPose animationPose = ((LivingEntityRenderStateAccess)livingEntityRenderState).animationOverhaul$getInterpolatedAnimationPose();


        // TODO: Will revisit this later once I continue work on third person animations.
        if(false){


            poseStack.popPose();
            poseStack.pushPose();


            if(livingEntityRenderState.pose == net.minecraft.world.entity.Pose.SLEEPING){

                Direction i = livingEntityRenderState.bedOrientation;
                float j = i != null ? sleepDirectionToRotation(i) : livingEntityRenderState.bodyRot;
                poseStack.mulPose(Axis.YP.rotationDegrees(j - 90));
            } else {

            }
            ci.cancel();
        }
    }

    @Redirect(method = "render(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V", ordinal = 0))
    private void removeBedTranslation(PoseStack instance, float f, float g, float h, LivingEntityRenderState livingEntityRenderState){
        //AnimationPose animationPose = ((LivingEntityRenderStateAccess)livingEntityRenderState).animationOverhaul$getInterpolatedAnimationPose();


        // TODO: Will revisit this later once I continue work on third person animations.
        if(false){

        } else {
            instance.translate(f, g, h);
        }
    }




    @Inject(method = "render(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V"))
    private void translateAndRotateAfterScale(R livingEntityRenderState, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, CallbackInfo ci){
        //AnimationPose animationPose = ((LivingEntityRenderStateAccess)livingEntityRenderState).animationOverhaul$getInterpolatedAnimationPose();


        // TODO: Will revisit this later once I continue work on third person animations.
        if(false){
            poseStack.translate(0, -1.5, 0);

            //String root = animationPose.getJointSkeleton().getRootJoint();

            //animationPose.getJointTransform(root).translateAndRotatePoseStack(poseStack);
            poseStack.translate(0, 1.5, 0);
        }


    }

     */

    @Unique
    private static float sleepDirectionToRotation(Direction direction) {
        return switch (direction) {
            case SOUTH -> 90.0f;
            case WEST -> 0.0f;
            case NORTH -> 270.0f;
            case EAST -> 180.0f;
            default -> 0.0f;
        };
    }
}
