package com.trainguy9512.locomotion.animation.animator.entity.firstperson;

import com.trainguy9512.locomotion.LocomotionMain;
import com.trainguy9512.locomotion.animation.driver.DriverKey;
import com.trainguy9512.locomotion.animation.driver.VariableDriver;
import com.trainguy9512.locomotion.animation.pose.LocalSpacePose;
import com.trainguy9512.locomotion.animation.pose.function.*;
import com.trainguy9512.locomotion.animation.pose.function.cache.CachedPoseContainer;
import com.trainguy9512.locomotion.render.ItemRenderType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

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
    public static final ResourceLocation BLOCK = register(LocomotionMain.makeResourceLocation("block"), GenericItemPoseDefinition.builder(
                    FirstPersonAnimationSequences.HAND_GENERIC_ITEM_BLOCK_POSE,
                    FirstPersonGenericItems::isBlockItem,
                    80)
            .setItemRenderType(ItemRenderType.BLOCK_STATE)
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

    private static boolean isBlockItem(ItemStack itemStack) {
        ResourceLocation identifier = BuiltInRegistries.ITEM.getKey(itemStack.getItem());
        ResourceLocation modelLocation = ResourceLocation.fromNamespaceAndPath(identifier.getNamespace(), "models/item/" + identifier.getPath() + ".json");
        return Minecraft.getInstance().getResourceManager().getResource(modelLocation).isEmpty();
    }

//    public static final List<Item> BLOCK_2D_OVERRIDE_ITEMS = List.of(
//            Items.IRON_BARS,
//            Items.GLASS_PANE,
//            Items.WHITE_STAINED_GLASS_PANE,
//            Items.ORANGE_STAINED_GLASS_PANE,
//            Items.MAGENTA_STAINED_GLASS_PANE,
//            Items.LIGHT_BLUE_STAINED_GLASS_PANE,
//            Items.YELLOW_STAINED_GLASS_PANE,
//            Items.LIME_STAINED_GLASS_PANE,
//            Items.PINK_STAINED_GLASS_PANE,
//            Items.GRAY_STAINED_GLASS_PANE,
//            Items.LIGHT_GRAY_STAINED_GLASS_PANE,
//            Items.CYAN_STAINED_GLASS_PANE,
//            Items.PURPLE_STAINED_GLASS_PANE,
//            Items.BLUE_STAINED_GLASS_PANE,
//            Items.BROWN_STAINED_GLASS_PANE,
//            Items.GREEN_STAINED_GLASS_PANE,
//            Items.RED_STAINED_GLASS_PANE,
//            Items.BLACK_STAINED_GLASS_PANE,
//            Items.POINTED_DRIPSTONE,
//            Items.SMALL_AMETHYST_BUD,
//            Items.MEDIUM_AMETHYST_BUD,
//            Items.LARGE_AMETHYST_BUD,
//            Items.AMETHYST_CLUSTER,
//            Items.RED_MUSHROOM,
//            Items.BROWN_MUSHROOM,
//            Items.DEAD_BUSH,
//            Items.SHORT_GRASS,
//            Items.TALL_GRASS,
//            Items.FERN,
//            Items.LARGE_FERN,
//            Items.CRIMSON_FUNGUS,
//            Items.WARPED_FUNGUS,
//            Items.BAMBOO,
//            Items.SUGAR_CANE,
//            Items.SMALL_DRIPLEAF,
//            Items.BIG_DRIPLEAF,
//            Items.CRIMSON_ROOTS,
//            Items.WARPED_ROOTS,
//            Items.NETHER_SPROUTS,
//            Items.WEEPING_VINES,
//            Items.TWISTING_VINES,
//            Items.VINE,
//            Items.GLOW_BERRIES,
//            Items.COCOA_BEANS,
//            Items.LILY_PAD,
//            Items.BRAIN_CORAL,
//            Items.BUBBLE_CORAL,
//            Items.HORN_CORAL,
//            Items.FIRE_CORAL,
//            Items.TUBE_CORAL,
//            Items.DEAD_BRAIN_CORAL,
//            Items.DEAD_BUBBLE_CORAL,
//            Items.DEAD_HORN_CORAL,
//            Items.DEAD_FIRE_CORAL,
//            Items.DEAD_TUBE_CORAL,
//            Items.BRAIN_CORAL_FAN,
//            Items.BUBBLE_CORAL_FAN,
//            Items.HORN_CORAL_FAN,
//            Items.FIRE_CORAL_FAN,
//            Items.TUBE_CORAL_FAN,
//            Items.DEAD_BRAIN_CORAL_FAN,
//            Items.DEAD_BUBBLE_CORAL_FAN,
//            Items.DEAD_HORN_CORAL_FAN,
//            Items.DEAD_FIRE_CORAL_FAN,
//            Items.DEAD_TUBE_CORAL_FAN,
//            Items.COBWEB,
//            Items.LILAC,
//            Items.PEONY,
//            Items.ROSE_BUSH,
//            Items.SUNFLOWER,
//            Items.MANGROVE_PROPAGULE,
//            Items.PINK_PETALS,
//            Items.PITCHER_PLANT,
//            Items.MELON_SEEDS,
//            Items.PUMPKIN_SEEDS,
//            Items.GLOW_LICHEN,
//            Items.SCULK_VEIN,
//            Items.NETHER_WART,
//            Items.SWEET_BERRIES,
//            Items.SEAGRASS,
//            Items.KELP,
//            Items.TORCH,
//            Items.SOUL_TORCH,
//            Items.REDSTONE_TORCH,
//            Items.BELL,
//            Items.LADDER,
//            Items.LIGHTNING_ROD,
//            Items.DECORATED_POT,
//            Items.REDSTONE,
//            Items.STRING,
//            Items.TRIPWIRE_HOOK,
//            Items.RAIL,
//            Items.ACTIVATOR_RAIL,
//            Items.DETECTOR_RAIL,
//            Items.POWERED_RAIL,
//            Items.FROGSPAWN,
//            //? >= 1.21.4 {
//            Items.PALE_HANGING_MOSS,
//            Items.RESIN_CLUMP,
//            //}
//            //? >= 1.21.5 {
//            Items.DRY_SHORT_GRASS,
//            Items.DRY_TALL_GRASS,
//            Items.BUSH,
//            Items.FIREFLY_BUSH,
//            Items.LEAF_LITTER,
//            Items.CACTUS_FLOWER,
//            Items.WILDFLOWERS
//            //}
//    );

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
