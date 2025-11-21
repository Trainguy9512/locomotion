package com.trainguy9512.locomotion.animation.animator.entity.firstperson;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMap;
import com.trainguy9512.locomotion.LocomotionMain;
import com.trainguy9512.locomotion.animation.data.OnTickDriverContainer;
import com.trainguy9512.locomotion.animation.pose.function.montage.MontageConfiguration;
import com.trainguy9512.locomotion.animation.pose.function.montage.MontageManager;
import com.trainguy9512.locomotion.util.TimeSpan;
import com.trainguy9512.locomotion.util.Transition;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class FirstPersonItemUpdateAnimations {

    private static final ArrayList<ItemUpdateAnimationRule> ITEM_UPDATE_ANIMATION_RULES = new ArrayList<>();

    static {
        registerItemUpdateAnimationRule(
                LocomotionMain.makeResourceLocation("crossbow_fire"),
                FirstPersonMontages::getCrossbowFireMontage,
                FirstPersonItemUpdateAnimations::shouldPlayCrossbowFire
        );
        registerItemUpdateAnimationRule(
                LocomotionMain.makeResourceLocation("bucket_collect"),
                FirstPersonMontages::getBucketCollectMontage,
                FirstPersonItemUpdateAnimations::shouldPlayBucketCollect
        );
        registerItemUpdateAnimationRule(
                LocomotionMain.makeResourceLocation("bucket_empty"),
                FirstPersonMontages::getBucketEmptyMontage,
                FirstPersonItemUpdateAnimations::shouldPlayBucketEmpty
        );
    }

    private static boolean shouldPlayCrossbowFire(ItemUpdateAnimationConditionContext context) {
        boolean bothItemsAreCrossbows = context.bothItemsMeetPredicate(itemStack -> itemStack.has(DataComponents.CHARGED_PROJECTILES));
        if (!bothItemsAreCrossbows) {
            return false;
        }
        boolean currentCrossbowHasCharge = !context.currentItem.get(DataComponents.CHARGED_PROJECTILES).isEmpty();
        boolean previousCrossbowHasCharge = !context.previousItem.get(DataComponents.CHARGED_PROJECTILES).isEmpty();
        return !currentCrossbowHasCharge && previousCrossbowHasCharge;
    }

    private static List<Item> FULL_BUCKET_ITEMS = List.of(
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

    public static void registerItemUpdateAnimationRule(
            ResourceLocation identifier,
            Function<InteractionHand, MontageConfiguration> montageProvider,
            Predicate<ItemUpdateAnimationConditionContext> shouldPlayAnimation
    ) {
        ITEM_UPDATE_ANIMATION_RULES.addFirst(new ItemUpdateAnimationRule(identifier, montageProvider, shouldPlayAnimation));
    }

    public record ItemUpdateAnimationRule(
            ResourceLocation identifier,
            Function<InteractionHand, MontageConfiguration> montageProvider,
            Predicate<ItemUpdateAnimationConditionContext> shouldPlayAnimation
    ) {

    }

    public record ItemUpdateAnimationConditionContext(
            ItemStack currentItem,
            ItemStack previousItem
    ) {
        boolean bothItemsMeetPredicate(Predicate<ItemStack> predicate) {
            return predicate.test(this.currentItem) && predicate.test(this.previousItem);
        }
    }

    public static void testForAndPlayItemUpdateAnimations(OnTickDriverContainer driverContainer, MontageManager montageManager, InteractionHand interactionHand) {
        if (driverContainer.getDriverValue(FirstPersonDrivers.HAS_DROPPED_ITEM)) {
            return;
        }
        if (driverContainer.getDriver(FirstPersonDrivers.HOTBAR_SLOT).hasValueChanged()) {
            return;
        }

        ItemStack currentItem = driverContainer.getDriver(FirstPersonDrivers.getItemCopyReferenceDriver(interactionHand)).getCurrentValue();
        ItemStack previousItem = driverContainer.getDriver(FirstPersonDrivers.getItemCopyReferenceDriver(interactionHand)).getPreviousValue();
        ItemStack renderedItem = driverContainer.getDriverValue(FirstPersonDrivers.getRenderedItemDriver(interactionHand));
        ItemUpdateAnimationConditionContext context = new ItemUpdateAnimationConditionContext(
                currentItem,
                previousItem
        );

        for (ItemUpdateAnimationRule rule : ITEM_UPDATE_ANIMATION_RULES) {
            if (rule.shouldPlayAnimation.test(context)) {
                LocomotionMain.DEBUG_LOGGER.info("Playing item update animation \"{}\"", rule.identifier);
                MontageConfiguration montage = rule.montageProvider.apply(interactionHand);
                for (String slot : montage.slots()) {
                    montageManager.interruptMontagesInSlot(slot, Transition.builder(TimeSpan.ofSeconds(0.2f)).build());
                }
                montageManager.playMontage(rule.montageProvider.apply(interactionHand));
                if (FirstPersonHandPose.fromItemStack(renderedItem) == FirstPersonHandPose.fromItemStack(currentItem)) {
                    FirstPersonDrivers.updateRenderedItem(driverContainer, interactionHand);
                }
                return;
            }
        }
    }

}
