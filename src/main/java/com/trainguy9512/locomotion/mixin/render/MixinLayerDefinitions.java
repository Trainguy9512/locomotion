package com.trainguy9512.locomotion.mixin.render;

import com.google.common.collect.ImmutableMap;
import net.minecraft.client.model.geom.LayerDefinitions;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(LayerDefinitions.class)
public class MixinLayerDefinitions {

    @Inject(
            method = "createRoots",
            at = @At(value = "RETURN")
    )
    private static void getCreatedModels(CallbackInfoReturnable<Map<ModelLayerLocation, LayerDefinition>> cir) {
        Map<ModelLayerLocation, LayerDefinition> models = cir.getReturnValue();
    }
}
