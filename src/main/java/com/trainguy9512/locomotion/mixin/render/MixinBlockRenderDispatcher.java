package com.trainguy9512.locomotion.mixin.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.trainguy9512.locomotion.access.AlternateSingleBlockRenderer;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.function.Supplier;


@Mixin(BlockRenderDispatcher.class)
public abstract class MixinBlockRenderDispatcher implements AlternateSingleBlockRenderer {

    @Shadow public abstract BlockStateModel getBlockModel(BlockState arg);

    @Shadow @Final private BlockColors blockColors;

    @Shadow @Final private Supplier<SpecialBlockModelRenderer> specialBlockModelRenderer;

    @Unique
    public void locomotion$renderSingleBlockWithEmission(BlockState blockState, PoseStack poseStack, MultiBufferSource bufferSource, SubmitNodeCollector nodeCollector, int combinedLight) {
        RenderShape renderShape = blockState.getRenderShape();
        // Don't do anything more if the render shape is nothing.
        if (renderShape == RenderShape.INVISIBLE) {
            return;
        }
        // Set the combined light integer to use the block's light emission if it is brighter than the current light level.
        combinedLight = LightTexture.lightCoordsWithEmission(combinedLight, blockState.getLightEmission());
        BlockStateModel blockStateModel = this.getBlockModel(blockState);
        int tint = this.blockColors.getColor(blockState, null, null, 0);
        float r = (float)(tint >> 16 & 0xFF) / 255.0f;
        float g = (float)(tint >> 8 & 0xFF) / 255.0f;
        float b = (float)(tint & 0xFF) / 255.0f;
        // Render each part of the block state model
        for (BlockModelPart blockModelPart : blockStateModel.collectParts(RandomSource.create(42L))) {
            for (Direction direction : Direction.values()) {
                for (BakedQuad bakedQuad : blockModelPart.getQuads(direction)) {
                    this.locomotion$renderBakedQuad(bakedQuad, poseStack, bufferSource, r, g, b, combinedLight, blockState);
                }
            }
            for (BakedQuad bakedQuad : blockModelPart.getQuads(null)) {
                this.locomotion$renderBakedQuad(bakedQuad, poseStack, bufferSource, r, g, b, combinedLight, blockState);
            }
        }
        // Render the block through the special block renderer if it has one (skulls, beds, banners)
        this.specialBlockModelRenderer.get().renderByBlock(blockState.getBlock(), ItemDisplayContext.NONE, poseStack, nodeCollector, combinedLight, OverlayTexture.NO_OVERLAY, 0);
    }

    @Unique
    private void locomotion$renderBakedQuad(BakedQuad bakedQuad, PoseStack poseStack, MultiBufferSource bufferSource, float r, float g, float b, int combinedLight, BlockState blockState) {
        if (bakedQuad.isTinted()) {
            r = Mth.clamp(r, 0.0f, 1.0f);
            g = Mth.clamp(g, 0.0f, 1.0f);
            b = Mth.clamp(b, 0.0f, 1.0f);
        } else {
            r = 1.0f;
            g = 1.0f;
            b = 1.0f;
        }
        // Use the chunk render type vertex consumer for baked quad parts that don't get shaded.
        VertexConsumer vertexConsumer;
        if (bakedQuad.shade()) {
            vertexConsumer = bufferSource.getBuffer(ItemBlockRenderTypes.getRenderType(blockState));
        } else {
            //? if <= 1.21.5 {
            /*vertexConsumer = bufferSource.getBuffer(ItemBlockRenderTypes.getChunkRenderType(blockState));
            *///?} else {
            vertexConsumer = bufferSource.getBuffer(RenderType.cutoutMipped());
            //?}
        }

        vertexConsumer.putBulkData(
                poseStack.last(),
                bakedQuad,
                new float[]{1, 1, 1, 1},
                r,
                g,
                b,
                1.0f,
                new int[]{combinedLight, combinedLight, combinedLight, combinedLight},
                OverlayTexture.NO_OVERLAY,
                true
        );
    }
}
