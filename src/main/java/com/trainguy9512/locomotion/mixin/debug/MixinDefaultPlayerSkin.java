package com.trainguy9512.locomotion.mixin.debug;

import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.entity.player.PlayerSkin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(DefaultPlayerSkin.class)
public abstract class MixinDefaultPlayerSkin {

    // Quick mixin for setting the default player skin rather than making it random.

    @Shadow @Final private static PlayerSkin[] DEFAULT_SKINS;

    @Shadow
    protected static PlayerSkin create(String name, PlayerModelType modelType) {
        return null;
    }

    @Inject(
            method = "get(Ljava/util/UUID;)Lnet/minecraft/world/entity/player/PlayerSkin;",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void makeSteveDefault(UUID uuid, CallbackInfoReturnable<PlayerSkin> cir) {
        cir.setReturnValue(create("entity/player/wide/sunny", PlayerModelType.WIDE));
    }
}
