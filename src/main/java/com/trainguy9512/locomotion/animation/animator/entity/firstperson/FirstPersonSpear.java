package com.trainguy9512.locomotion.animation.animator.entity.firstperson;

import com.trainguy9512.locomotion.animation.data.OnTickDriverContainer;
import com.trainguy9512.locomotion.animation.pose.LocalSpacePose;
import com.trainguy9512.locomotion.animation.pose.function.PoseFunction;
import com.trainguy9512.locomotion.animation.pose.function.cache.CachedPoseContainer;
import com.trainguy9512.locomotion.animation.pose.function.montage.MontageManager;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.KineticWeapon;

public class FirstPersonSpear {

    public static PoseFunction<LocalSpacePose> constructSpearPoseFunction(CachedPoseContainer cachedPoseContainer, InteractionHand hand) {
        return FirstPersonMining.makeMainHandPickaxeMiningPoseFunction(cachedPoseContainer, hand);
    }

    public static void extractSpearData(LocalPlayer player, OnTickDriverContainer driverContainer, MontageManager montageManager) {
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack itemStack = player.getItemInHand(hand);
            KineticWeapon kineticWeapon = itemStack.get(DataComponents.KINETIC_WEAPON);
            if (kineticWeapon == null) {
                continue;
            }
            int spearUseDuration = itemStack.getUseDuration(player) - (player.getUseItemRemainingTicks() + 1);
            int delayTicks = kineticWeapon.delayTicks();
            driverContainer.getDriver(FirstPersonDrivers.SPEAR_CAN_DISMOUNT).setValue(spearUseDuration < kineticWeapon.dismountConditions().map(KineticWeapon.Condition::maxDurationTicks).orElse(0).floatValue() - delayTicks);
            driverContainer.getDriver(FirstPersonDrivers.SPEAR_CAN_KNOCKBACK).setValue(spearUseDuration < kineticWeapon.knockbackConditions().map(KineticWeapon.Condition::maxDurationTicks).orElse(0).floatValue() - delayTicks);
            driverContainer.getDriver(FirstPersonDrivers.SPEAR_CAN_DAMAGE).setValue(spearUseDuration < kineticWeapon.damageConditions().map(KineticWeapon.Condition::maxDurationTicks).orElse(0).floatValue() - delayTicks);
//            driverContainer.getDriver(FirstPersonDrivers.SPEAR_COOLDOWN).setValue();


        }
    }

}
