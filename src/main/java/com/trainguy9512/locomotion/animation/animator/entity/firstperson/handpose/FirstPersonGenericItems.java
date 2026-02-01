package com.trainguy9512.locomotion.animation.animator.entity.firstperson.handpose;

import com.trainguy9512.locomotion.LocomotionMain;
import com.trainguy9512.locomotion.animation.animator.entity.firstperson.FirstPersonAnimationSequences;
import com.trainguy9512.locomotion.animation.animator.entity.firstperson.FirstPersonMining;
import com.trainguy9512.locomotion.animation.pose.LocalSpacePose;
import com.trainguy9512.locomotion.animation.pose.function.*;
import com.trainguy9512.locomotion.animation.pose.function.cache.CachedPoseContainer;
import com.trainguy9512.locomotion.render.ItemRenderType;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class FirstPersonGenericItems {

    private static final Map<Identifier, GenericItemPoseDefinition> GENERIC_ITEM_POSES_BY_IDENTIFIER = new HashMap<>();

    public static Identifier register(Identifier identifier, GenericItemPoseDefinition genericItemPoseDefinition) {
        GENERIC_ITEM_POSES_BY_IDENTIFIER.put(identifier, genericItemPoseDefinition);
        return identifier;
    }

//    public static final Identifier GENERIC_2D_ITEM = register(LocomotionMain.makeIdentifier("generic_2d_item"), GenericItemPoseDefinition.builder(
//            FirstPersonAnimationSequences.HAND_GENERIC_ITEM_2D_ITEM_POSE,
//            itemStack -> true,
//            0)
//            .build());
    public static final Identifier FIXED_ITEM = register(LocomotionMain.makeIdentifier("fixed_item"), GenericItemPoseDefinition.builder(
                    FirstPersonAnimationSequences.HAND_GENERIC_ITEM_FIXED_ITEM_POSE,
                    itemStack -> true,
                    0)
            .setItemRenderType(ItemRenderType.FIXED)
            .build());
    public static final Identifier ROD = register(LocomotionMain.makeIdentifier("rod"), GenericItemPoseDefinition.builder(
            FirstPersonAnimationSequences.HAND_GENERIC_ITEM_ROD_POSE,
            FirstPersonGenericItems::isRodItem,
            20)
            .build());
    public static final Identifier SHEARS = register(LocomotionMain.makeIdentifier("shears"), GenericItemPoseDefinition.builder(
            FirstPersonAnimationSequences.HAND_GENERIC_ITEM_SHEARS_POSE,
            FirstPersonGenericItems::isShearsItem,
            60)
            .build());
    public static final Identifier FISHING_ROD = register(LocomotionMain.makeIdentifier("fishing_rod"), GenericItemPoseDefinition.builder(
            FirstPersonAnimationSequences.HAND_GENERIC_ITEM_FISHING_ROD_POSE,
            FirstPersonGenericItems::isFishingRodItem,
            70)
            .build());
    public static final Identifier ARROW = register(LocomotionMain.makeIdentifier("arrow"), GenericItemPoseDefinition.builder(
            FirstPersonAnimationSequences.HAND_GENERIC_ITEM_ARROW_POSE,
            FirstPersonGenericItems::isArrowItem,
            80)
            .setItemRenderType(ItemRenderType.MIRRORED_THIRD_PERSON_ITEM)
            .build());
//    public static final Identifier BLOCK = register(LocomotionMain.makeIdentifier("block"), GenericItemPoseDefinition.builder(
//                    FirstPersonAnimationSequences.HAND_GENERIC_ITEM_BLOCK_POSE,
//                    FirstPersonGenericItems::isBlockItem,
//                    80)
//            .setItemRenderType(ItemRenderType.BLOCK_STATE)
//            .build());
//    public static final Identifier DOOR = register(LocomotionMain.makeIdentifier("door"), GenericItemPoseDefinition.builder(
//                    FirstPersonAnimationSequences.HAND_GENERIC_ITEM_DOOR_BLOCK_POSE,
//                    FirstPersonGenericItems::isDoorItem,
//                    90)
//            .setItemRenderType(ItemRenderType.BLOCK_STATE)
//            .build());

    public static final List<Item> ROD_ITEMS = List.of(
            Items.BONE,
            Items.STICK,
            Items.BLAZE_ROD,
            Items.BREEZE_ROD,
            Items.POINTED_DRIPSTONE,
            Items.BAMBOO,
            Items.DEBUG_STICK,
            Items.END_ROD
    );

    private static boolean isRodItem(ItemStack itemStack) {
        return ROD_ITEMS.contains(itemStack.getItem());
    }

    private static boolean isShearsItem(ItemStack itemStack) {
        return itemStack.getItem() instanceof ShearsItem;
    }

    public static final List<Item> FISHING_ROD_ITEMS = List.of(
            Items.FISHING_ROD,
            Items.CARROT_ON_A_STICK,
            Items.WARPED_FUNGUS_ON_A_STICK
    );

    private static boolean isFishingRodItem(ItemStack itemStack) {
        return FISHING_ROD_ITEMS.contains(itemStack.getItem());
    }

    private static boolean isArrowItem(ItemStack itemStack) {
        return itemStack.is(ItemTags.ARROWS);
    }

    public static final List<Item> BLOCK_ITEM_OVERRIDES = List.of(
            Items.CHEST,
            Items.TRAPPED_CHEST,
            Items.ENDER_CHEST
    );

    public static final List<TagKey<Item>> BLOCK_ITEM_TAG_OVERRIDES = List.of(
            ItemTags.COPPER_CHESTS,
            ItemTags.SHULKER_BOXES,
            ItemTags.SKULLS,
            ItemTags.BEDS
    );

    private static boolean isBlockItem(ItemStack itemStack) {
        if (!(itemStack.getItem() instanceof BlockItem)) {
            return false;
        }
        for (Item item : BLOCK_ITEM_OVERRIDES) {
            if (itemStack.is(item)) {
                return true;
            }
        }
        for (TagKey<Item> tag : BLOCK_ITEM_TAG_OVERRIDES) {
            if (itemStack.is(tag)) {
                return true;
            }
        }
        Identifier identifier = BuiltInRegistries.ITEM.getKey(itemStack.getItem());
        Identifier modelLocation = Identifier.fromNamespaceAndPath(identifier.getNamespace(), "models/item/" + identifier.getPath() + ".json");
        return Minecraft.getInstance().getResourceManager().getResource(modelLocation).isEmpty();
    }

    private static boolean isDoorItem(ItemStack itemStack) {
        return itemStack.is(ItemTags.DOORS);
    }

    public record GenericItemPoseDefinition(
            Identifier basePoseSequence,
            Predicate<ItemStack> choosePoseIfTrue,
            int evaluationPriority,
            ItemRenderType itemRenderType
    ) {

        public static Builder builder(
                Identifier basePoseAnimationSequence,
                Predicate<ItemStack> choosePoseIfTrue,
                int evaluationPriority
        ) {
            return new Builder(basePoseAnimationSequence, choosePoseIfTrue, evaluationPriority);
        }

        public static class Builder {

            private final Identifier basePoseAnimationSequence;
            private final int evaluationPriority;
            private final Predicate<ItemStack> choosePoseIfTrue;
            private ItemRenderType itemRenderType;

            private Builder(
                    Identifier basePoseAnimationSequence,
                    Predicate<ItemStack> choosePoseIfTrue,
                    int evaluationPriority
            ) {
                this.basePoseAnimationSequence = basePoseAnimationSequence;
                this.choosePoseIfTrue = choosePoseIfTrue;
                this.evaluationPriority = evaluationPriority;
                this.itemRenderType = ItemRenderType.THIRD_PERSON_ITEM;
            }

            public Builder setItemRenderType(ItemRenderType itemRenderType) {
                this.itemRenderType = itemRenderType;
                return this;
            }

            public GenericItemPoseDefinition build() {
                return new GenericItemPoseDefinition(
                        basePoseAnimationSequence,
                        choosePoseIfTrue,
                        evaluationPriority,
                        itemRenderType
                );
            }
        }
    }

    public static Identifier getFallback() {
        return FIXED_ITEM;
    }

    public static GenericItemPoseDefinition getOrThrowFromIdentifier(Identifier identifier) {
        GenericItemPoseDefinition definition = GENERIC_ITEM_POSES_BY_IDENTIFIER.get(identifier);
        if (definition == null) {
            throw new RuntimeException("Identifier " + identifier + " is not a registered generic item pose.");
        }
        return definition;
    }

    public static Identifier getConfigurationFromItem(ItemStack itemStack) {
        Map<Identifier, GenericItemPoseDefinition> genericItemPosesSortedByPriority = GENERIC_ITEM_POSES_BY_IDENTIFIER.entrySet()
                .stream()
                .sorted(Comparator.comparingInt(entry -> -entry.getValue().evaluationPriority()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue,
                        LinkedHashMap::new
                ));
        for (Identifier key : genericItemPosesSortedByPriority.keySet()) {
            GenericItemPoseDefinition definition = GENERIC_ITEM_POSES_BY_IDENTIFIER.get(key);
            if (definition.choosePoseIfTrue().test(itemStack)) {
                return key;
            }
        }
        // Fallback if nothing passes
        return getFallback();
    }

//    public static Identifier getGenericItemPoseSequence(PoseFunction.FunctionInterpolationContext context, InteractionHand hand) {
//        DriverKey<VariableDriver<Identifier>> driver = FirstPersonDrivers.getGenericItemPoseDriver(hand);
//        Identifier genericItemPoseIdentifier = context.driverContainer().getInterpolatedDriverValue(driver, context.partialTicks());
//        GenericItemPoseDefinition definition = getOrThrowFromIdentifier(genericItemPoseIdentifier);
//        return definition.basePoseSequence;
//    }

    public static PoseFunction<LocalSpacePose> constructPoseFunction(CachedPoseContainer cachedPoseContainer, InteractionHand hand) {
        PoseFunction<LocalSpacePose> pose = FirstPersonMining.constructMainHandPickaxeMiningPoseFunction(cachedPoseContainer, hand);

        PoseFunction<LocalSpacePose> consumablePose;
        consumablePose = SequenceEvaluatorFunction.builder(FirstPersonAnimationSequences.HAND_GENERIC_ITEM_2D_ITEM_POSE).build();
        consumablePose = FirstPersonEating.constructWithEatingStateMachine(cachedPoseContainer, hand, consumablePose);
        consumablePose = FirstPersonDrinking.constructWithDrinkingStateMachine(cachedPoseContainer, hand, consumablePose);

        pose = ApplyAdditiveFunction.of(
                pose,
                MakeDynamicAdditiveFunction.of(
                        consumablePose,
                        SequenceEvaluatorFunction.builder(FirstPersonAnimationSequences.HAND_GENERIC_ITEM_2D_ITEM_POSE).build()
                )
        );

        return pose;
    }
}
