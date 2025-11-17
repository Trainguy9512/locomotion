package com.trainguy9512.locomotion.animation.animator.entity.firstperson;

import com.trainguy9512.locomotion.LocomotionMain;
import com.trainguy9512.locomotion.animation.data.OnTickDriverContainer;
import com.trainguy9512.locomotion.animation.driver.TriggerDriver;
import com.trainguy9512.locomotion.animation.pose.function.montage.MontageConfiguration;
import com.trainguy9512.locomotion.animation.pose.function.montage.MontageManager;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.function.Function;
import java.util.function.Predicate;

public class FirstPersonUseAnimations {

    private static final ArrayList<UseAnimationRule> USE_ANIMATION_RULES = new ArrayList<>();

    static {
        registerUseAnimationRule(
                LocomotionMain.makeResourceLocation("default"),
                FirstPersonMontages::getUseAnimationMontage,
                context -> context.clientSwingSource == InteractionResult.SwingSource.CLIENT
        );
        registerUseAnimationRule(
                LocomotionMain.makeResourceLocation("crossbow_fire"),
                FirstPersonMontages::getCrossbowFireMontage,
                FirstPersonUseAnimations::shouldPlayCrossbowFire
        );
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

    private static boolean shouldPlayCrossbowFire(UseAnimationConditionContext context) {
        boolean bothItemsAreCrossbows = context.bothItemsMeetCondition(itemStack -> itemStack.has(DataComponents.CHARGED_PROJECTILES));
        if (!bothItemsAreCrossbows) {
            return false;
        }
        boolean currentCrossbowHasCharge = !context.currentItem.get(DataComponents.CHARGED_PROJECTILES).isEmpty();
        boolean previousCrossbowHasCharge = !context.previousItem.get(DataComponents.CHARGED_PROJECTILES).isEmpty();
        return !currentCrossbowHasCharge && previousCrossbowHasCharge;
    }

    private static boolean shouldPlayAxeScrape(UseAnimationConditionContext context) {
        boolean isAxeItem = context.bothItemsMeetCondition(itemStack -> itemStack.is(ItemTags.AXES));
        if (!isAxeItem) {
            return false;
        }
        return context.hasDurabilityChanged();
    }

    private static boolean shouldPlayHoeTill(UseAnimationConditionContext context) {
        boolean isHoeItem = context.bothItemsMeetCondition(itemStack -> itemStack.is(ItemTags.HOES));
        if (!isHoeItem) {
            return false;
        }
        return context.hasDurabilityChanged();
    }

    private static boolean shouldPlayShovelFlatten(UseAnimationConditionContext context) {
        boolean isShovelItem = context.bothItemsMeetCondition(itemStack -> itemStack.is(ItemTags.SHOVELS));
        if (!isShovelItem) {
            return false;
        }
        return context.hasDurabilityChanged();
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
            InteractionResult.SwingSource clientSwingSource,
            ItemStack currentItem,
            ItemStack previousItem
    ) {
        public boolean bothItemsMeetCondition(Predicate<ItemStack> predicate) {
            return predicate.test(this.currentItem) && predicate.test(this.previousItem);
        }

        public boolean hasDurabilityChanged() {
            boolean bothItemsCanBeDamaged = this.bothItemsMeetCondition(ItemStack::isDamageableItem);
            if (!bothItemsCanBeDamaged) {
                return false;
            }
            int currentItemDamage = this.currentItem.getDamageValue();
            int previousItemDamage = this.previousItem.getDamageValue();
            return currentItemDamage - 1 == previousItemDamage;
        }
    }

    public enum UseAnimationType {
        INTERACT,
        INTERACT_AT,
        USE_ITEM,
        USE_ITEM_ON;

        UseAnimationType() {

        }
    }

    public static void scheduleAndPlayUseAnimation(OnTickDriverContainer driverContainer, MontageManager montageManager, InteractionHand interactionHand) {
        // Scheduling the next use animation if triggered by the multiplayer game mode.
        TriggerDriver hasUsedItemDriver = driverContainer.getDriver(FirstPersonDrivers.getHasUsedItemDriver(interactionHand));
        long currentTick = driverContainer.getCurrentTick();
        hasUsedItemDriver.runAndConsumeIfTriggered(() -> {
            driverContainer.getDriver(FirstPersonDrivers.SCHEDULED_USE_ANIMATION_TICK).setValue(currentTick + 1);
        });

        // If no use animation is scheduled, return.
        long scheduledUseAnimationTick = driverContainer.getDriverValue(FirstPersonDrivers.SCHEDULED_USE_ANIMATION_TICK);
        if (scheduledUseAnimationTick != currentTick) {
            return;
        }
        InteractionHand lastUsedHand = driverContainer.getDriverValue(FirstPersonDrivers.LAST_USED_HAND);
        if (lastUsedHand != interactionHand) {
            return;
        }

        ItemStack renderedItem = driverContainer.getDriverValue(FirstPersonDrivers.getRenderedItemDriver(interactionHand));
        ItemStack actualItem = driverContainer.getDriverValue(FirstPersonDrivers.getItemDriver(interactionHand));
        UseAnimationType useAnimationType = driverContainer.getDriverValue(FirstPersonDrivers.LAST_USED_TYPE);
        InteractionResult.SwingSource swingSource = driverContainer.getDriverValue(FirstPersonDrivers.LAST_USED_SWING_SOURCE);
        UseAnimationConditionContext context = new UseAnimationConditionContext(
                useAnimationType,
                swingSource,
                actualItem,
                renderedItem
        );

//        LocomotionMain.DEBUG_LOGGER.info("{}, {}",
//                actualItem.getDamageValue(),
//                renderedItem.getDamageValue()
//        );

        for (UseAnimationRule rule : USE_ANIMATION_RULES) {
            if (rule.shouldChooseUseAnimation.test(context)) {
//                LocomotionMain.DEBUG_LOGGER.info("Playing use animation \"{}\"", rule.identifier);
                montageManager.playMontage(rule.montageProvider.apply(interactionHand));
                if (FirstPersonHandPose.fromItemStack(renderedItem) == FirstPersonHandPose.fromItemStack(actualItem)) {
                    FirstPersonDrivers.updateRenderedItem(driverContainer, interactionHand);
                }
                hasUsedItemDriver.consume();
                return;
            }
        }
        hasUsedItemDriver.consume();

//        if (FirstPersonHandPose.fromItemStack(renderedItem) == FirstPersonHandPose.fromItemStack(actualItem)) {
//            LocomotionMain.DEBUG_LOGGER.info("{}, {}",
//                    actualItem,
//                    renderedItem
//            );
//            FirstPersonDrivers.updateRenderedItem(driverContainer, interactionHand);
//        }
    }

}
