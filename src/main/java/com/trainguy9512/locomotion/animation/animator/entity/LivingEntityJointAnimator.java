//? if >= 1.21.9 {
package com.trainguy9512.locomotion.animation.animator.entity;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;

public interface LivingEntityJointAnimator<T extends LivingEntity, S extends LivingEntityRenderState> extends EntityJointAnimator<T, S> {
}
//?} else {
/*package com.trainguy9512.locomotion.animation.animator.entity;

import net.minecraft.world.entity.LivingEntity;

public interface LivingEntityJointAnimator<T extends LivingEntity> extends EntityJointAnimator<T> {
}
*///?}
