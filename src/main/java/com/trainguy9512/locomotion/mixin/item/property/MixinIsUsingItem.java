package com.trainguy9512.locomotion.mixin.item.property;

import com.trainguy9512.locomotion.animation.animator.JointAnimatorDispatcher;
import com.trainguy9512.locomotion.animation.animator.entity.firstperson.FirstPersonDrivers;
import com.trainguy9512.locomotion.render.FirstPersonPlayerRenderer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.conditional.IsUsingItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(IsUsingItem.class)
public class MixinIsUsingItem {

    @Inject(
            method = "get",
            at = @At("HEAD"),
            cancellable = true
    )
    public void injectCustomCrossbowPull(ItemStack itemStack, ClientLevel clientLevel, LivingEntity livingEntity, int i, ItemDisplayContext itemDisplayContext, CallbackInfoReturnable<Boolean> cir) {
        if (FirstPersonPlayerRenderer.IS_RENDERING_LOCOMOTION_FIRST_PERSON) {
            JointAnimatorDispatcher.getInstance().getFirstPersonPlayerDataContainer().ifPresent(dataContainer -> {
                cir.setReturnValue(dataContainer.getInterpolatedDriverValue(
                        FirstPersonDrivers.getUsingItemPropertyDriverKey(FirstPersonPlayerRenderer.CURRENT_ITEM_INTERACTION_HAND),
                        FirstPersonPlayerRenderer.CURRENT_PARTIAL_TICKS
                ));
            });
        }
    }
}
