package com.trainguy9512.locomotion.mixin.item;

//? if >= 1.21.5 {

import com.trainguy9512.locomotion.animation.animator.JointAnimatorDispatcher;
import com.trainguy9512.locomotion.animation.animator.entity.firstperson.FirstPersonDrivers;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.component.BlocksAttacks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlocksAttacks.class)
public class MixinBlocksAttacks {

    @Inject(
            method = "onBlocked",
            at = @At("HEAD")
    )
    public void playShieldImpactMontageOnShieldBlocked(ServerLevel serverLevel, LivingEntity livingEntity, CallbackInfo ci) {
        if (Minecraft.getInstance().isLocalPlayer(livingEntity.getUUID())) {
            JointAnimatorDispatcher.getInstance().getFirstPersonPlayerDataContainer().ifPresent(dataContainer -> dataContainer.getDriver(FirstPersonDrivers.HAS_BLOCKED_ATTACK).trigger());
        }
    }
}

//?} else {

/*import net.minecraft.world.item.ShieldItem;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ShieldItem.class)
public class MixinBlocksAttacks {

}

*///?}