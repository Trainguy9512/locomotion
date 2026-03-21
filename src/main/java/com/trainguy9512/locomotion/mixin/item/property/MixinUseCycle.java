//? if >= 1.21.0 {
package com.trainguy9512.locomotion.mixin.item.property;

import com.trainguy9512.locomotion.animation.animator.JointAnimatorDispatcher;
import com.trainguy9512.locomotion.animation.animator.entity.firstperson.FirstPersonDrivers;
import com.trainguy9512.locomotion.render.FirstPersonPlayerRenderer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.numeric.UseCycle;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(UseCycle.class)
public class MixinUseCycle {

    @Inject(
            method = "get",
            at = @At("HEAD"),
            cancellable = true
    )
    public void injectLocomotionUseCycle(ItemStack itemStack, ClientLevel clientLevel, LivingEntity livingEntity, int i, ItemDisplayContext itemDisplayContext, CallbackInfoReturnable<Float> cir) {
        if (FirstPersonPlayerRenderer.IS_RENDERING_LOCOMOTION_FIRST_PERSON) {
            JointAnimatorDispatcher.getInstance().getInterpolatedFirstPersonPlayerPose().ifPresent(pose -> {
                JointAnimatorDispatcher.getInstance().getFirstPersonPlayerDataContainer().ifPresent(driverContainer -> {
                    InteractionHand hand = FirstPersonPlayerRenderer.CURRENT_ITEM_INTERACTION_HAND;
                    InteractionHand currentUsingInteractionHand = driverContainer.getDriver(FirstPersonDrivers.LAST_USED_HAND).getCurrentValue();
                    cir.setReturnValue(hand == currentUsingInteractionHand ? pose.getCustomAttributeValue("use_cycle_property") : 0f);
                });
            });
        }
    }
}
//?} else {
/*package com.trainguy9512.locomotion.mixin.item.property;

import net.minecraft.client.renderer.item.ItemProperties;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ItemProperties.class)
public class MixinUseCycle {
}
*///?}
