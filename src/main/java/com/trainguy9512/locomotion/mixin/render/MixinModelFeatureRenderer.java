package com.trainguy9512.locomotion.mixin.render;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.trainguy9512.locomotion.animation.animator.JointAnimatorDispatcher;
import com.trainguy9512.locomotion.animation.data.AnimationDataContainer;
import com.trainguy9512.locomotion.animation.pose.Pose;
import com.trainguy9512.locomotion.render.LocomotionWrappedRenderState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Optional;

@Mixin(ModelFeatureRenderer.class)
public class MixinModelFeatureRenderer<S> {

    @WrapOperation(
            method = "renderModel",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/Model;setupAnim(Ljava/lang/Object;)V")
    )
    public void redirectSetupAnim(Model<S> instance, S renderState, Operation<Void> original, @Local(argsOnly = true) SubmitNodeStorage.ModelSubmit<S> modelSubmit) {
        if (renderState instanceof LocomotionWrappedRenderState<?> wrappedRenderState) {
            original.call(instance, wrappedRenderState.getInnerValue());
            Optional<AnimationDataContainer> potentialDataContainer = wrappedRenderState.getDataContainer();
            if (potentialDataContainer.isPresent()) {
                AnimationDataContainer dataContainer = potentialDataContainer.get();
                float partialTicks = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true);
                Pose pose = dataContainer.computePose(partialTicks);
                JointAnimatorDispatcher.getInstance().setupAnimWithAnimationPose(instance, pose);
            }
        } else {
            original.call(instance, renderState);
        }
    }

}
