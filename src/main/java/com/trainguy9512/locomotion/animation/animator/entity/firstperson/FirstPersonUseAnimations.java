package com.trainguy9512.locomotion.animation.animator.entity.firstperson;

import com.trainguy9512.locomotion.LocomotionMain;
import com.trainguy9512.locomotion.animation.data.OnTickDriverContainer;
import com.trainguy9512.locomotion.animation.driver.TriggerDriver;
import com.trainguy9512.locomotion.animation.pose.function.montage.MontageConfiguration;
import com.trainguy9512.locomotion.animation.pose.function.montage.MontageManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.function.Function;
import java.util.function.Predicate;

public class FirstPersonUseAnimations {

    private static final ArrayList<UseAnimationRule> USE_ANIMATION_RULES = new ArrayList<>();

    static {
        registerUseAnimationRule(
                LocomotionMain.makeResourceLocation("default"),
                FirstPersonMontages::getUseAnimationMontage,
                UseAnimationConditionContext::isTargetingBlockOrEntity
        );
//        registerUseAnimationRule(
//                LocomotionMain.makeResourceLocation("crossbow_fire"),
//                FirstPersonMontages::getCrossbowFireMontage,
//                FirstPersonUseAnimations::shouldPlayCrossbowFire
//        );
        registerUseAnimationRule(
                LocomotionMain.makeResourceLocation("axe_scrape"),
                FirstPersonMontages::getAxeScrapeMontage,
                FirstPersonUseAnimations::shouldPlayAxeScrape
        );
        registerUseAnimationRule(
                LocomotionMain.makeResourceLocation("hoe_till"),
                FirstPersonMontages::getHoeTillMontage,
                FirstPersonUseAnimations::shouldPlayHoeTill
        );
        registerUseAnimationRule(
                LocomotionMain.makeResourceLocation("shovel_flatten"),
                FirstPersonMontages::getShovelFlattenMontage,
                FirstPersonUseAnimations::shouldPlayShovelFlatten
        );
    }

//    private static boolean shouldPlayCrossbowFire(UseAnimationConditionContext context) {
//        boolean bothItemsAreCrossbows = context.bothItemsMeetCondition(itemStack -> itemStack.has(DataComponents.CHARGED_PROJECTILES));
//        if (!bothItemsAreCrossbows) {
//            return false;
//        }
//        boolean currentCrossbowHasCharge = !context.currentItem.get(DataComponents.CHARGED_PROJECTILES).isEmpty();
//        boolean previousCrossbowHasCharge = !context.previousItem.get(DataComponents.CHARGED_PROJECTILES).isEmpty();
//        return !currentCrossbowHasCharge && previousCrossbowHasCharge;
//    }

    private static boolean shouldPlayAxeScrape(UseAnimationConditionContext context) {
        return context.currentItem.is(ItemTags.AXES) && context.isTargetingBlock();
    }

    private static boolean shouldPlayHoeTill(UseAnimationConditionContext context) {
        return context.currentItem.is(ItemTags.HOES) && context.isTargetingBlock();
    }

    private static boolean shouldPlayShovelFlatten(UseAnimationConditionContext context) {
        return context.currentItem.is(ItemTags.SHOVELS) && context.isTargetingBlock();
    }

    public static void registerUseAnimationRule(
            ResourceLocation identifier,
            Function<InteractionHand, MontageConfiguration> montageProvider,
            Predicate<UseAnimationConditionContext> shouldChooseUseAnimation
    ) {
        USE_ANIMATION_RULES.addFirst(new UseAnimationRule(identifier, montageProvider, shouldChooseUseAnimation));
    }

    public record UseAnimationRule(
            ResourceLocation identifier,
            Function<InteractionHand, MontageConfiguration> montageProvider,
            Predicate<UseAnimationConditionContext> shouldChooseUseAnimation
    ) {

    }

    public record UseAnimationConditionContext(
            UseAnimationType useAnimationType,
            ItemStack currentItem,
            boolean isTargetingBlock,
            boolean isTargetingEntity
    ) {

        public boolean isTargetingBlockOrEntity() {
            return this.isTargetingBlock || this.isTargetingEntity;
        }

//        public boolean hasDurabilityChanged() {
//            boolean bothItemsCanBeDamaged = this.bothItemsMeetCondition(ItemStack::isDamageableItem);
//            if (!bothItemsCanBeDamaged) {
//                return false;
//            }
//            int currentItemDamage = this.currentItem.getDamageValue();
//            return currentItemDamage - 1 == previousItemDamage;
//        }
    }

    public enum UseAnimationType {
        INTERACT,
        INTERACT_AT,
        USE_ITEM,
        USE_ITEM_ON;

        UseAnimationType() {

        }
    }

    public static void playUseAnimationIfTriggered(OnTickDriverContainer driverContainer, MontageManager montageManager, InteractionHand interactionHand) {
        // Scheduling the next use animation if triggered by the multiplayer game mode.
        TriggerDriver hasUsedItemDriver = driverContainer.getDriver(FirstPersonDrivers.getHasUsedItemDriver(interactionHand));
        TriggerDriver hasAttackedDriver = driverContainer.getDriver(FirstPersonDrivers.HAS_ATTACKED);

        int swingTime = driverContainer.getDriverValue(FirstPersonDrivers.LAST_USED_SWING_TIME);
        InteractionHand lastUsedHand = driverContainer.getDriverValue(FirstPersonDrivers.LAST_USED_HAND);
        boolean swingTimeIsOne = swingTime == 1;

        if (lastUsedHand != interactionHand) {
            return;
        }
        boolean shouldPlayUseAnimationThisTick = hasUsedItemDriver.hasBeenTriggeredAndNotConsumed() || (swingTimeIsOne && hasAttackedDriver.hasBeenTriggered());
        if (!shouldPlayUseAnimationThisTick) {
            return;
        }
        hasUsedItemDriver.consume();

        ItemStack renderedItem = driverContainer.getDriverValue(FirstPersonDrivers.getRenderedItemDriver(interactionHand));
        ItemStack actualItem = driverContainer.getDriverValue(FirstPersonDrivers.getItemDriver(interactionHand));
        UseAnimationType useAnimationType = driverContainer.getDriverValue(FirstPersonDrivers.LAST_USE_TYPE);
        UseAnimationConditionContext context = new UseAnimationConditionContext(
                useAnimationType,
                actualItem,
                useAnimationType == UseAnimationType.USE_ITEM_ON,
                useAnimationType == UseAnimationType.INTERACT || useAnimationType == UseAnimationType.INTERACT_AT
        );

        for (UseAnimationRule rule : USE_ANIMATION_RULES) {
            if (rule.shouldChooseUseAnimation.test(context)) {
                LocomotionMain.DEBUG_LOGGER.info("Playing use animation \"{}\"", rule.identifier);
                montageManager.playMontage(rule.montageProvider.apply(interactionHand));
                if (FirstPersonHandPose.fromItemStack(renderedItem) == FirstPersonHandPose.fromItemStack(actualItem)) {
                    FirstPersonDrivers.updateRenderedItem(driverContainer, interactionHand);
                }
                return;
            }
        }
    }
}
