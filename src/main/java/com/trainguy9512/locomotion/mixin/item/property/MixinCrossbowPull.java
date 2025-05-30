package com.trainguy9512.locomotion.mixin.item.property;

import com.trainguy9512.locomotion.animation.animator.JointAnimatorDispatcher;
import com.trainguy9512.locomotion.animation.animator.entity.firstperson.FirstPersonDrivers;
import com.trainguy9512.locomotion.render.FirstPersonPlayerRenderer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.numeric.CrossbowPull;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CrossbowPull.class)
public class MixinCrossbowPull {

    /**
     * Modifies the "Crossbow Pull" item model property to sync up with Locomotion's first person animations rather than how it's calculated in vanilla.
     */
    @Inject(
            method = "get",
            at = @At("HEAD"),
            cancellable = true
    )
    public void injectLocomotionCrossbowPull(ItemStack stack, ClientLevel level, LivingEntity entity, int seed, CallbackInfoReturnable<Float> cir) {
        if (FirstPersonPlayerRenderer.IS_RENDERING_LOCOMOTION_FIRST_PERSON) {
            JointAnimatorDispatcher.getInstance().getInterpolatedFirstPersonPlayerPose().ifPresent(pose -> {
                JointAnimatorDispatcher.getInstance().getFirstPersonPlayerDataContainer().ifPresent(driverContainer -> {
                    InteractionHand interactionHand = FirstPersonPlayerRenderer.CURRENT_ITEM_INTERACTION_HAND;
                    InteractionHand currentUsingInteractionHand = driverContainer.getDriver(FirstPersonDrivers.LAST_USED_HAND).getCurrentValue();
                    cir.setReturnValue(interactionHand == currentUsingInteractionHand ? pose.getCustomAttributeValue("crossbow_pull_property") : 0f);
                });
            });
        }
    }
}
