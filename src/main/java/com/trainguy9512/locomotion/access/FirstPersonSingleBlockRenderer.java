package com.trainguy9512.locomotion.access;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.level.block.state.BlockState;

public interface FirstPersonSingleBlockRenderer {
    void locomotion$submitSingleBlockWithEmission(BlockState blockState, PoseStack poseStack, SubmitNodeCollector nodeCollector, int combinedLight);
}
