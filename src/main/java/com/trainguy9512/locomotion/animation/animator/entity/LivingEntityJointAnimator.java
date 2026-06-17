package com.trainguy9512.locomotion.animation.animator.entity;

import com.trainguy9512.locomotion.animation.driver.DriverKey;
import com.trainguy9512.locomotion.animation.driver.VariableDriver;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;

public interface LivingEntityJointAnimator<T extends LivingEntity, S extends LivingEntityRenderState> extends EntityJointAnimator<T, S> {

    public static final DriverKey<VariableDriver<Float>> ENTITY_ROTATION = DriverKey.of("entity_rotation", () -> VariableDriver.ofFloat(() -> 0f));
    public static final String ENTITY_ROTATION_ATTRIBUTE = "entity_rotation";

}
