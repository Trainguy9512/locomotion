package com.trainguy9512.locomotion.animation.animator.entity.firstperson;

import com.google.common.collect.Maps;
import com.trainguy9512.locomotion.LocomotionMain;
import com.trainguy9512.locomotion.animation.data.OnTickDriverContainer;
import com.trainguy9512.locomotion.animation.pose.function.montage.MontageConfiguration;
import com.trainguy9512.locomotion.animation.pose.function.montage.MontageManager;
import com.trainguy9512.locomotion.animation.util.TimeSpan;
import com.trainguy9512.locomotion.animation.util.Transition;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class FirstPersonItemUpdateAnimations {

    private static final Map<Identifier, ItemUpdateAnimationRule> ITEM_UPDATE_ANIMATION_RULES_BY_IDENTIFIER = Maps.newHashMap();

    public static final Identifier CROSSBOW_FIRE = register(LocomotionMain.makeIdentifier("crossbow_fire"), ItemUpdateAnimationRule.of(
            FirstPersonMontages::getCrossbowFireMontage,
            FirstPersonItemUpdateAnimations::shouldPlayCrossbowFire,
            0
    ));
    public static final Identifier BUCKET_COLLECT = register(LocomotionMain.makeIdentifier("bucket_collect"), ItemUpdateAnimationRule.of(
            FirstPersonMontages::getBucketCollectMontage,
            FirstPersonItemUpdateAnimations::shouldPlayBucketCollect,
            0
    ));
    public static final Identifier BUCKET_EMPTY = register(LocomotionMain.makeIdentifier("bucket_empty"), ItemUpdateAnimationRule.of(
            FirstPersonMontages::getBucketEmptyMontage,
            FirstPersonItemUpdateAnimations::shouldPlayBucketEmpty,
            0
    ));

    private static boolean shouldPlayCrossbowFire(ItemUpdateAnimationConditionContext context) {
        boolean bothItemsAreCrossbows = context.bothItemsMeetPredicate(itemStack -> itemStack.has(DataComponents.CHARGED_PROJECTILES));
        if (!bothItemsAreCrossbows) {
            return false;
        }
        boolean currentCrossbowHasCharge = !context.currentItem.get(DataComponents.CHARGED_PROJECTILES).isEmpty();
        boolean previousCrossbowHasCharge = !context.previousItem.get(DataComponents.CHARGED_PROJECTILES).isEmpty();
        return !currentCrossbowHasCharge && previousCrossbowHasCharge;
    }

    private static final List<Item> FULL_BUCKET_ITEMS = List.of(
            Items.WATER_BUCKET,
            Items.LAVA_BUCKET,
            Items.AXOLOTL_BUCKET,
            Items.COD_BUCKET,
            Items.POWDER_SNOW_BUCKET,
            Items.PUFFERFISH_BUCKET,
            Items.SALMON_BUCKET,
            Items.TADPOLE_BUCKET,
            Items.TROPICAL_FISH_BUCKET
    );

    private static boolean shouldPlayBucketCollect(ItemUpdateAnimationConditionContext context) {
        boolean previousItemIsEmptyBucket = context.previousItem().is(Items.BUCKET);
        boolean currentItemIsCollectedBucket = FULL_BUCKET_ITEMS.contains(context.currentItem().getItem());
        return previousItemIsEmptyBucket && currentItemIsCollectedBucket;
    }

    private static boolean shouldPlayBucketEmpty(ItemUpdateAnimationConditionContext context) {
        boolean currentItemIsEmptyBucket = context.currentItem().is(Items.BUCKET);
        boolean previousItemIsCollectedBucket = FULL_BUCKET_ITEMS.contains(context.previousItem().getItem());
        return currentItemIsEmptyBucket && previousItemIsCollectedBucket;
    }

    public static Identifier register(Identifier identifier, ItemUpdateAnimationRule itemUpdateAnimationRule) {
        ITEM_UPDATE_ANIMATION_RULES_BY_IDENTIFIER.put(identifier, itemUpdateAnimationRule);
        return identifier;
    }

    public record ItemUpdateAnimationRule(
            Function<InteractionHand, MontageConfiguration> montageProvider,
            Predicate<ItemUpdateAnimationConditionContext> shouldPlayAnimation,
            int evaluationPriority
    ) {
        public static ItemUpdateAnimationRule of(
                Function<InteractionHand, MontageConfiguration> montageProvider,
                Predicate<ItemUpdateAnimationConditionContext> shouldPlayAnimation,
                int evaluationPriority
        ) {
            return new ItemUpdateAnimationRule(montageProvider, shouldPlayAnimation, evaluationPriority);
        }
    }

    public record ItemUpdateAnimationConditionContext(
            ItemStack currentItem,
            ItemStack previousItem
    ) {
        boolean bothItemsMeetPredicate(Predicate<ItemStack> predicate) {
            return predicate.test(this.currentItem) && predicate.test(this.previousItem);
        }
    }

    public static void testForAndPlayItemUpdateAnimations(OnTickDriverContainer driverContainer, MontageManager montageManager, InteractionHand hand) {
        if (driverContainer.getDriverValue(FirstPersonDrivers.HAS_DROPPED_ITEM)) {
            return;
        }
        if (driverContainer.getDriver(FirstPersonDrivers.HOTBAR_SLOT).hasValueChanged()) {
            return;
        }
        // Don't play the item update animations if the player is currently in a screen
        if (driverContainer.getDriverValue(FirstPersonDrivers.HAS_SCREEN_OPEN)) {
            return;
        }

        ItemStack actualItem = driverContainer.getDriver(FirstPersonDrivers.getItemCopyReferenceDriver(hand)).getCurrentValue();
        ItemStack previousItem = driverContainer.getDriver(FirstPersonDrivers.getItemCopyReferenceDriver(hand)).getPreviousValue();
        ItemStack renderedItem = driverContainer.getDriverValue(FirstPersonDrivers.getRenderedItemDriver(hand));
        ItemUpdateAnimationConditionContext context = new ItemUpdateAnimationConditionContext(
                actualItem,
                previousItem
        );

        Map<Identifier, ItemUpdateAnimationRule> sortedItemUpdateAnimationRules = ITEM_UPDATE_ANIMATION_RULES_BY_IDENTIFIER.entrySet()
                .stream()
                .sorted(Comparator.comparingInt(entry -> -entry.getValue().evaluationPriority()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue,
                        LinkedHashMap::new
                ));

        for (Identifier ruleIdentifier : sortedItemUpdateAnimationRules.keySet()) {
            ItemUpdateAnimationRule rule = sortedItemUpdateAnimationRules.get(ruleIdentifier);
            if (rule.shouldPlayAnimation.test(context)) {
                LocomotionMain.DEBUG_LOGGER.info("Playing item update animation \"{}\"", ruleIdentifier);
                MontageConfiguration montage = rule.montageProvider.apply(hand);
                for (String slot : montage.slots()) {
                    montageManager.interruptMontagesInSlot(slot, Transition.builder(TimeSpan.ofSeconds(0.2f)).build());
                }
                montageManager.playMontage(rule.montageProvider.apply(hand));
                Identifier renderedItemHandPose = FirstPersonHandPoses.testForNextHandPose(renderedItem, hand);
                Identifier currentItemHandPose = FirstPersonHandPoses.testForNextHandPose(actualItem, hand);
                if (currentItemHandPose == renderedItemHandPose) {
                    FirstPersonDrivers.updateRenderedItem(driverContainer, hand);
                }
                return;
            }
        }
    }

}
