package com.trainguy9512.locomotion.animation.animator.entity.firstperson;

import com.trainguy9512.locomotion.LocomotionMain;
import com.trainguy9512.locomotion.animation.driver.DriverKey;
import com.trainguy9512.locomotion.animation.driver.VariableDriver;
import com.trainguy9512.locomotion.animation.pose.LocalSpacePose;
import com.trainguy9512.locomotion.animation.pose.function.*;
import com.trainguy9512.locomotion.animation.pose.function.cache.CachedPoseContainer;
import com.trainguy9512.locomotion.render.ItemRenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class FirstPersonGenericItems {

    private static final Map<ResourceLocation, GenericItemPoseDefinition> GENERIC_ITEM_POSES_BY_LOCATION = new HashMap<>();

    public static ResourceLocation register(ResourceLocation identifier, GenericItemPoseDefinition genericItemPoseDefinition) {
        GENERIC_ITEM_POSES_BY_LOCATION.put(identifier, genericItemPoseDefinition);
        return identifier;
    }

    public static final ResourceLocation GENERIC_2D_ITEM = register(LocomotionMain.makeResourceLocation("generic_2d_item"), GenericItemPoseDefinition.builder(
            FirstPersonAnimationSequences.HAND_GENERIC_ITEM_2D_ITEM_POSE,
            itemStack -> true,
            0)
            .build());
    public static final ResourceLocation ROD = register(LocomotionMain.makeResourceLocation("rod"), GenericItemPoseDefinition.builder(
            FirstPersonAnimationSequences.HAND_GENERIC_ITEM_ROD_POSE,
            FirstPersonGenericItems::isRodItem,
            20)
            .build());
    public static final ResourceLocation SHEARS = register(LocomotionMain.makeResourceLocation("shears"), GenericItemPoseDefinition.builder(
            FirstPersonAnimationSequences.HAND_GENERIC_ITEM_SHEARS_POSE,
            FirstPersonGenericItems::isShearsItem,
            60)
            .build());
    public static final ResourceLocation FISHING_ROD = register(LocomotionMain.makeResourceLocation("fishing_rod"), GenericItemPoseDefinition.builder(
            FirstPersonAnimationSequences.HAND_GENERIC_ITEM_FISHING_ROD_POSE,
            FirstPersonGenericItems::isFishingRodItem,
            70)
            .build());
    public static final ResourceLocation ARROW = register(LocomotionMain.makeResourceLocation("arrow"), GenericItemPoseDefinition.builder(
            FirstPersonAnimationSequences.HAND_GENERIC_ITEM_ARROW_POSE,
            FirstPersonGenericItems::isArrowItem,
            80)
            .setItemRenderType(ItemRenderType.MIRRORED_THIRD_PERSON_ITEM)
            .build());

    private static final List<Item> ROD_ITEMS = List.of(
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
        return itemStack.is(Items.SHEARS);
    }

    private static final List<Item> FISHING_ROD_ITEMS = List.of(
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

    public record GenericItemPoseDefinition(
            ResourceLocation basePoseAnimationSequence,
            Predicate<ItemStack> usePoseCondition,
            int evaluationPriority,
            ItemRenderType itemRenderType
    ) {

        public static Builder builder(
                ResourceLocation basePoseAnimationSequence,
                Predicate<ItemStack> usePoseCondition,
                int evaluationPriority
        ) {
            return new Builder(basePoseAnimationSequence, usePoseCondition, evaluationPriority);
        }

        public static class Builder {

            private final ResourceLocation basePoseAnimationSequence;
            private final int evaluationPriority;
            private final Predicate<ItemStack> usePoseCondition;
            private ItemRenderType itemRenderType;

            private Builder(
                    ResourceLocation basePoseAnimationSequence,
                    Predicate<ItemStack> usePoseCondition,
                    int evaluationPriority
            ) {
                this.basePoseAnimationSequence = basePoseAnimationSequence;
                this.usePoseCondition = usePoseCondition;
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
                        usePoseCondition,
                        evaluationPriority,
                        itemRenderType
                );
            }
        }
    }

    public static ResourceLocation getFallback() {
        return GENERIC_2D_ITEM;
    }

    public static GenericItemPoseDefinition getOrThrowFromIdentifier(ResourceLocation identifier) {
        return GENERIC_ITEM_POSES_BY_LOCATION.get(identifier);
    }

    public static ResourceLocation getConfigurationFromItem(ItemStack itemStack) {
        Map<ResourceLocation, GenericItemPoseDefinition> genericItemPosesSortedByPriority = GENERIC_ITEM_POSES_BY_LOCATION.entrySet()
                .stream()
                .sorted(Comparator.comparingInt(entry -> -entry.getValue().evaluationPriority()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue,
                        LinkedHashMap::new
                ));
        for (ResourceLocation key : genericItemPosesSortedByPriority.keySet()) {
            GenericItemPoseDefinition definition = GENERIC_ITEM_POSES_BY_LOCATION.get(key);
            if (definition.usePoseCondition().test(itemStack)) {
                return key;
            }
        }
        // Fallback if nothing passes
        return getFallback();
    }

    public static ResourceLocation getGenericItemPoseSequence(PoseFunction.FunctionInterpolationContext context, InteractionHand interactionHand) {
        DriverKey<VariableDriver<ResourceLocation>> driver = FirstPersonDrivers.getGenericItemPoseDriver(interactionHand);
        ResourceLocation genericItemPoseIdentifier = context.driverContainer().getInterpolatedDriverValue(driver, context.partialTicks());
        GenericItemPoseDefinition definition = getOrThrowFromIdentifier(genericItemPoseIdentifier);
        return definition.basePoseAnimationSequence;
    }

    public static PoseFunction<LocalSpacePose> constructPoseFunction(CachedPoseContainer cachedPoseContainer, InteractionHand interactionHand) {
        PoseFunction<LocalSpacePose> pose = SequenceEvaluatorFunction.builder(context -> getGenericItemPoseSequence(context, interactionHand)).build();

        if (interactionHand == InteractionHand.MAIN_HAND) {
            PoseFunction<LocalSpacePose> additiveMiningPose = MakeDynamicAdditiveFunction.of(
                    FirstPersonMining.makePickaxeMiningPoseFunction(cachedPoseContainer),
                    SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_TOOL_POSE).build());
            pose = ApplyAdditiveFunction.of(pose, additiveMiningPose);
        }

        PoseFunction<LocalSpacePose> consumablePose;
        consumablePose = SequenceEvaluatorFunction.builder(FirstPersonAnimationSequences.HAND_GENERIC_ITEM_2D_ITEM_POSE).build();
        consumablePose = FirstPersonEating.constructWithEatingStateMachine(cachedPoseContainer, interactionHand, consumablePose);
        consumablePose = FirstPersonDrinking.constructWithDrinkingStateMachine(cachedPoseContainer, interactionHand, consumablePose);

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
