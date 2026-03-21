package com.trainguy9512.locomotion.access;

import com.mojang.blaze3d.vertex.PoseStack;
//? if >= 1.21.9 {
import net.minecraft.client.renderer.SubmitNodeCollector;
//?} else {
/*import net.minecraft.client.renderer.MultiBufferSource;*/
//?}
import net.minecraft.world.level.block.state.BlockState;

public interface FirstPersonSingleBlockRenderer {
    //? if >= 1.21.9 {
    void locomotion$submitSingleBlockWithEmission(BlockState blockState, PoseStack poseStack, SubmitNodeCollector nodeCollector, int combinedLight);
    //?} else {
    /*void locomotion$renderSingleBlockWithEmission(BlockState blockState, PoseStack poseStack, MultiBufferSource bufferSource, int combinedLight);*/
    //?}
}
