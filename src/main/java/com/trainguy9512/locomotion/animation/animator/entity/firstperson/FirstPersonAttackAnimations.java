package com.trainguy9512.locomotion.animation.animator.entity.firstperson;

import com.trainguy9512.locomotion.animation.pose.function.montage.MontageConfiguration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Function;
import java.util.function.Predicate;

public class FirstPersonAttackAnimations {

    public record AttackAnimationRule(
            ResourceLocation identifier,
            MontageConfiguration montageConfiguration,
            Predicate<AttackAnimationConditionContext> shouldChooseAttackAnimation
    ) {

    }

    public record AttackAnimationConditionContext(
            ItemStack currentItem
    ) {

    }
}
