package com.trainguy9512.locomotion.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.trainguy9512.locomotion.access.FirstPersonSingleBlockRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class FirstPersonBlockItemRenderer {

    private static List<BlockRenderConfiguration> BLOCK_RENDER_CONFIGURATIONS = new ArrayList<>();

    public static void register(BlockRenderConfiguration blockRenderConfiguration) {
        BLOCK_RENDER_CONFIGURATIONS.add(blockRenderConfiguration);
        BLOCK_RENDER_CONFIGURATIONS = new ArrayList<>(BLOCK_RENDER_CONFIGURATIONS.stream()
                .sorted(Comparator.comparingInt(BlockRenderConfiguration::evaluationPriority).reversed())
                .toList());
    }


    static {
        register(BlockRenderConfiguration.of(
                FirstPersonBlockItemRenderer::renderSingleBlock,
                block -> true,
                0
        ));
        register(BlockRenderConfiguration.of(
                FirstPersonBlockItemRenderer::renderDoorBlock,
                block -> block instanceof DoorBlock,
                10
        ));
    }

    public static void renderSingleBlock(BlockSubmitContext context) {
        submitSingleBlock(context.nodeCollector(), context.poseStack(), context.blockState(), context.combinedLight());
    }

    public static void renderDoorBlock(BlockSubmitContext context) {
        submitSingleBlock(context.nodeCollector(), context.poseStack(), context.blockState(), context.combinedLight());
        renderUpperHalfBlock(context);
    }

    private static void renderUpperHalfBlock(BlockSubmitContext context) {
        context.poseStack().translate(0, 1, 0);
        BlockState upperHalfBlockstate = context.blockState().setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER);
        submitSingleBlock(context.nodeCollector(), context.poseStack(), upperHalfBlockstate, context.combinedLight());
    }


    public record BlockRenderConfiguration(
            Consumer<BlockSubmitContext> renderer,
            Predicate<Block> shouldChoose,
            int evaluationPriority
    ) {
        public static BlockRenderConfiguration of(
                Consumer<BlockSubmitContext> renderer,
                Predicate<Block> shouldChoose,
                int evaluationPriority
        ) {
            return new BlockRenderConfiguration(
                    renderer,
                    shouldChoose,
                    evaluationPriority
            );
        }
    }

    public record BlockSubmitContext(
            BlockState blockState,
            PoseStack poseStack,
            SubmitNodeCollector nodeCollector,
            int combinedLight,
            HumanoidArm side
    ) {

    }

    public static void submit(
            ItemStack itemStack,
            PoseStack poseStack,
            SubmitNodeCollector nodeCollector,
            int combinedLight,
            HumanoidArm side
    ) {
        if (!(itemStack.getItem() instanceof BlockItem blockItem)) {
            return;
        }
        if (side == HumanoidArm.LEFT) {
            poseStack.translate(-1, 0, 0);
        }
        Block block = blockItem.getBlock();
        BlockState blockState = block.defaultBlockState();
        for (BlockRenderConfiguration configuration : BLOCK_RENDER_CONFIGURATIONS) {
            if (configuration.shouldChoose().test(block)) {
                configuration.renderer.accept(new BlockSubmitContext(
                        blockState,
                        poseStack,
                        nodeCollector,
                        combinedLight,
                        side
                ));
                return;
            }
        }
    }

    private static void submitSingleBlock(SubmitNodeCollector nodeCollector, PoseStack poseStack, BlockState blockState, int combinedLight) {
        ((FirstPersonSingleBlockRenderer) Minecraft.getInstance().getBlockRenderer()).locomotion$submitSingleBlockWithEmission(blockState, poseStack, nodeCollector, combinedLight);
    }

}
