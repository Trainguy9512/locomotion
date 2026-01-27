package com.trainguy9512.locomotion.mixin.game;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.trainguy9512.locomotion.animation.animator.JointAnimatorDispatcher;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LevelChunk.class)
public class MixinLevelChunk {

    @WrapOperation(
            method = "updateBlockEntityTicker",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getTicker(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/block/entity/BlockEntityType;)Lnet/minecraft/world/level/block/entity/BlockEntityTicker;")
    )
    public <T extends BlockEntity> BlockEntityTicker<T> tickExtraOnClient(BlockState instance, Level level, BlockEntityType<T> blockEntityType, Operation<BlockEntityTicker<T>> original) {
        BlockEntityTicker<T> ticker = original.call(instance, level, blockEntityType);
//        if (ticker == null) {
//            return;
//        }
        if (!level.isClientSide()) {
            return ticker;
        }
        return ((tickerLevel, tickerBlockPos, tickerState, tickerBlockEntity) -> {
            if (ticker != null) {
                ticker.tick(tickerLevel, tickerBlockPos, tickerState, tickerBlockEntity);
            }
            JointAnimatorDispatcher.getInstance().tickBlockEntityJointAnimator(tickerLevel, tickerBlockPos, tickerState, tickerBlockEntity);
        });
    }
}
