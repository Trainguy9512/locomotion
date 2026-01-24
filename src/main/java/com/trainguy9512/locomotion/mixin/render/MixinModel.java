package com.trainguy9512.locomotion.mixin.render;

import com.trainguy9512.locomotion.access.ModelDataContainerStorage;
import com.trainguy9512.locomotion.animation.animator.JointAnimatorDispatcher;
import com.trainguy9512.locomotion.animation.data.AnimationDataContainer;
import com.trainguy9512.locomotion.animation.pose.Pose;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Model.class)
public class MixinModel<S> implements ModelDataContainerStorage {

    @Unique
    private AnimationDataContainer locomotion$dataContainer;

    @Override
    public void locomotion$setDataContainer(AnimationDataContainer container) {
        this.locomotion$dataContainer = container;
    }

    @Override
    public AnimationDataContainer locomotion$getDataContainer() {
        return this.locomotion$dataContainer;
    }
}
