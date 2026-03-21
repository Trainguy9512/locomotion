//? if >= 1.21.9 {
package com.trainguy9512.locomotion.mixin.item;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ItemInHandLayer.class)
public abstract class MixinItemInHandLayer<T extends LivingEntity, S extends ArmedEntityRenderState, M extends EntityModel<S>> extends RenderLayer<S, M> {

    public MixinItemInHandLayer(RenderLayerParent<S, M> renderLayerParent) {
        super(renderLayerParent);
    }

    private boolean shouldTransformItemInHand(LivingEntityRenderState livingEntityRenderState){
        return false;
    }
}
//?} else {
/*package com.trainguy9512.locomotion.mixin.item;

import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ItemInHandLayer.class)
public abstract class MixinItemInHandLayer {
}
*///?}
