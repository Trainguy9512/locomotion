//? if >= 1.21.9 {
package com.trainguy9512.locomotion.mixin.render.layer;

import com.trainguy9512.locomotion.access.LivingEntityRenderStateAccess;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.layers.WingsLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(WingsLayer.class)
public abstract class MixinElytraLayer<T extends LivingEntity, S extends HumanoidRenderState, M extends EntityModel<S>> extends RenderLayer<S, M> {
    public MixinElytraLayer(RenderLayerParent<S, M> renderLayerParent) {
        super(renderLayerParent);
    }

    private boolean isValidForElytraTransformation(LivingEntityRenderState livingEntityRenderState){
        return ((LivingEntityRenderStateAccess)livingEntityRenderState).animationOverhaul$getInterpolatedAnimationPose() != null;
    }
}
//?} else {
/*package com.trainguy9512.locomotion.mixin.render.layer;

import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ElytraLayer.class)
public abstract class MixinElytraLayer {
}
*///?}
