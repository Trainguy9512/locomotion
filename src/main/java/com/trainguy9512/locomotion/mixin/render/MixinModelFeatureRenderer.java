package com.trainguy9512.locomotion.mixin.render;

import com.llamalad7.mixinextras.sugar.Local;
import com.trainguy9512.locomotion.LocomotionMain;
import com.trainguy9512.locomotion.access.ModelDataContainerStorage;
import com.trainguy9512.locomotion.animation.animator.JointAnimatorDispatcher;
import com.trainguy9512.locomotion.animation.data.AnimationDataContainer;
import com.trainguy9512.locomotion.animation.pose.Pose;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ModelFeatureRenderer.class)
public class MixinModelFeatureRenderer<S> {

    @Redirect(
            method = "renderModel",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/Model;setupAnim(Ljava/lang/Object;)V")
    )
    public void redirectSetupAnim(Model<S> instance, S renderState, @Local(argsOnly = true) SubmitNodeStorage.ModelSubmit<S> modelSubmit) {
        instance.setupAnim(renderState);
        AnimationDataContainer container = ((ModelDataContainerStorage)instance).locomotion$getDataContainer();
        if (container != null) {
            Pose pose = container.computePose(Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true));
            JointAnimatorDispatcher.getInstance().setupAnimWithAnimationPose(instance, pose);
        }
    }

}
