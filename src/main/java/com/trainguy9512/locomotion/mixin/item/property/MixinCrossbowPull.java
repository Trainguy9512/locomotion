package com.trainguy9512.locomotion.mixin.item.property;

import com.trainguy9512.locomotion.animation.animator.JointAnimatorDispatcher;
import com.trainguy9512.locomotion.animation.animator.entity.firstperson.FirstPersonDrivers;
import com.trainguy9512.locomotion.animation.data.AnimationDataContainer;
import com.trainguy9512.locomotion.animation.data.OnTickDriverContainer;
import com.trainguy9512.locomotion.animation.data.PoseCalculationDataContainer;
import com.trainguy9512.locomotion.render.FirstPersonPlayerRenderer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.numeric.CrossbowPull;
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
    public void injectCustomCrossbowPull(ItemStack stack, ClientLevel level, LivingEntity entity, int seed, CallbackInfoReturnable<Float> cir) {
        if (FirstPersonPlayerRenderer.IS_RENDERING_LOCOMOTION_FIRST_PERSON) {
            PoseCalculationDataContainer dataContainer = JointAnimatorDispatcher.getInstance().getFirstPersonPlayerDataContainer().get();
            if (dataContainer == null) {
                // bleh
            }
            cir.setReturnValue(1f);
        }
    }
}
