package com.trainguy9512.locomotion.mixin.render;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.trainguy9512.locomotion.access.ModelDataContainerStorage;
import com.trainguy9512.locomotion.animation.animator.JointAnimatorDispatcher;
import com.trainguy9512.locomotion.animation.data.AnimationDataContainer;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.ChestRenderer;
import net.minecraft.client.renderer.blockentity.state.ChestRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(ChestRenderer.class)
public class MixinChestRenderer {

    @WrapOperation(
            method = "submit(Lnet/minecraft/client/renderer/blockentity/state/ChestRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitModel(Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/rendertype/RenderType;IIILnet/minecraft/client/renderer/texture/TextureAtlasSprite;ILnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V")
    )
    public void setChestModelDataContainers(
            SubmitNodeCollector instance,
            Model<?> model,
            Object o,
            PoseStack poseStack,
            RenderType renderType,
            int i,
            int j,
            int k,
            TextureAtlasSprite textureAtlasSprite,
            int f,
            ModelFeatureRenderer.CrumblingOverlay crumblingOverlay,
            Operation<Void> original,
            @Local(argsOnly = true)
            ChestRenderState renderState
    ) {
        Optional<AnimationDataContainer> potentialContainer = JointAnimatorDispatcher.getInstance().getBlockEntityAnimationDataContainer(renderState.blockPos, renderState.blockEntityType);
        if (potentialContainer.isPresent()) {
            AnimationDataContainer container = potentialContainer.get();
            ((ModelDataContainerStorage) model).locomotion$setDataContainer(container);
        }
        original.call(instance, model, o, poseStack, renderType, i, j, k, textureAtlasSprite, f, crumblingOverlay);
    }
}
