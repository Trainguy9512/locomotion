package com.trainguy9512.locomotion.mixin.render;

import com.llamalad7.mixinextras.sugar.Local;
import com.trainguy9512.locomotion.access.FirstPersonPlayerRendererGetter;
import com.trainguy9512.locomotion.render.FirstPersonPlayerRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(EntityRenderDispatcher.class)
public class MixinEntityRenderDispatcher implements FirstPersonPlayerRendererGetter {

    @Unique
    private FirstPersonPlayerRenderer locomotion$firstPersonPlayerRenderer;

    @Inject(
            method = "onResourceManagerReload",
            at = @At("TAIL")
    )
    private void constructLocomotionFirstPersonPlayerRenderer(ResourceManager resourceManager, CallbackInfo ci, @Local EntityRendererProvider.Context context){
        this.locomotion$firstPersonPlayerRenderer = new FirstPersonPlayerRenderer(context);
    }

    @Override
    public Optional<FirstPersonPlayerRenderer> locomotion$getFirstPersonPlayerRenderer() {
        return Optional.ofNullable(this.locomotion$firstPersonPlayerRenderer);
    }
}
