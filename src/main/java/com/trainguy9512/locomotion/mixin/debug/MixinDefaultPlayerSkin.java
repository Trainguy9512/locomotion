package com.trainguy9512.locomotion.mixin.debug;

import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.PlayerSkin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(DefaultPlayerSkin.class)
public abstract class MixinDefaultPlayerSkin {


    @Shadow @Final private static PlayerSkin[] DEFAULT_SKINS;

    @Inject(
            method = "get(Ljava/util/UUID;)Lnet/minecraft/client/resources/PlayerSkin;",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void makeSteveDefault(UUID uuid, CallbackInfoReturnable<PlayerSkin> cir) {
        cir.setReturnValue(DEFAULT_SKINS[11]);
    }
}
