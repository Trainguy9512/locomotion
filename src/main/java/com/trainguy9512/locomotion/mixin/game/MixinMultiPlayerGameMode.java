package com.trainguy9512.locomotion.mixin.game;

import com.trainguy9512.locomotion.animation.animator.JointAnimatorDispatcher;
import com.trainguy9512.locomotion.animation.animator.entity.firstperson.FirstPersonDrivers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public class MixinMultiPlayerGameMode {

    @Shadow @Final private Minecraft minecraft;

    @Inject(
            method = "stopDestroyBlock",
            at = @At("HEAD")
    )
    public void disableMiningAnimationOnNoLongerMining(CallbackInfo ci) {
        assert this.minecraft.player != null;
        if (!this.minecraft.player.getAbilities().instabuild) {
            JointAnimatorDispatcher.getInstance().getFirstPersonPlayerDataContainer().ifPresent(dataContainer -> {
//                if (driverContainer.getDriver(FirstPersonPlayerJointAnimator.IS_MINING).getCurrentValue() && !driverContainer.getDriver(FirstPersonPlayerJointAnimator.IS_MINING).getPreviousValue()) {
//                    driverContainer.getDriver(FirstPersonPlayerJointAnimator.HAS_ATTACKED).trigger();
//                }
                if (dataContainer.getDriver(FirstPersonDrivers.IS_MINING).getPreviousValue()) {
                    dataContainer.getDriver(FirstPersonDrivers.IS_MINING).setValue(false);
                }
            });
        }
    }

//    @Inject(
//            method = "useItem",
//            at = @At("RETURN")
//    )
//    public void playUseItemMontage(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
//        if (cir.getReturnValue() instanceof InteractionResult.Success) {
//            JointAnimatorDispatcher.getInstance().getFirstPersonPlayerDataContainer().ifPresent(dataContainer -> {
//                dataContainer.getDriver(FirstPersonDrivers.getHasUsedItemDriver(hand)).trigger();
//            });
//        }
//    }

    @Inject(
            method = "startDestroyBlock",
            at = @At("HEAD")
    )
    public void enableMiningAnimationOnBeginMining(BlockPos loc, Direction face, CallbackInfoReturnable<Boolean> cir) {
        assert this.minecraft.player != null;
        if (!this.minecraft.player.getAbilities().instabuild) {
            JointAnimatorDispatcher.getInstance().getFirstPersonPlayerDataContainer().ifPresent(dataContainer -> dataContainer.getDriver(FirstPersonDrivers.IS_MINING).setValue(true));
        }
    }

    @Inject(
            method = "continueDestroyBlock",
            at = @At("HEAD")
    )
    public void enableMiningAnimationOnContinueMining(BlockPos loc, Direction face, CallbackInfoReturnable<Boolean> cir) {
        assert this.minecraft.player != null;
        if (!this.minecraft.player.getAbilities().instabuild) {
            JointAnimatorDispatcher.getInstance().getFirstPersonPlayerDataContainer().ifPresent(dataContainer -> dataContainer.getDriver(FirstPersonDrivers.IS_MINING).setValue(true));
        }
    }

    @Inject(
            method = "destroyBlock",
            at = @At("RETURN")
    )
    public void destroyBlockInCreativeInstantly(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        assert this.minecraft.player != null;
        if (cir.getReturnValue() && this.minecraft.player.getAbilities().instabuild) {
            JointAnimatorDispatcher.getInstance().getFirstPersonPlayerDataContainer().ifPresent(dataContainer -> dataContainer.getDriver(FirstPersonDrivers.HAS_ATTACKED).trigger());
        }
    }
}
