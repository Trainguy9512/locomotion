package com.trainguy9512.locomotion.animation.animator.entity.firstperson;

import com.trainguy9512.locomotion.LocomotionMain;
import com.trainguy9512.locomotion.animation.pose.LocalSpacePose;
import com.trainguy9512.locomotion.animation.pose.function.*;
import com.trainguy9512.locomotion.animation.pose.function.cache.CachedPoseContainer;
import com.trainguy9512.locomotion.util.Easing;
import com.trainguy9512.locomotion.util.TimeSpan;
import com.trainguy9512.locomotion.util.Transition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

public enum FirstPersonGenericItemPose {
    DEFAULT_2D_ITEM (FirstPersonAnimationSequences.HAND_GENERIC_ITEM_2D_ITEM_POSE, false, false),
    BLOCK (FirstPersonAnimationSequences.HAND_GENERIC_ITEM_BLOCK_POSE, true, false),
    SMALL_BLOCK (FirstPersonAnimationSequences.HAND_GENERIC_ITEM_SMALL_BLOCK_POSE, true, false),
    ROD (FirstPersonAnimationSequences.HAND_GENERIC_ITEM_ROD_POSE, false, false),
    DOOR_BLOCK (FirstPersonAnimationSequences.HAND_GENERIC_ITEM_DOOR_BLOCK_POSE, true, false),
    BANNER (FirstPersonAnimationSequences.HAND_GENERIC_ITEM_BANNER_POSE, false, false),
    ARROW (FirstPersonAnimationSequences.HAND_GENERIC_ITEM_ARROW_POSE, false, true);

    public final ResourceLocation basePoseLocation;
    public final boolean rendersBlockState;
    public final boolean rendersMirrored;

    FirstPersonGenericItemPose(ResourceLocation basePoseLocation, boolean rendersBlockState, boolean rendersMirrored) {
        this.basePoseLocation = basePoseLocation;
        this.rendersBlockState = rendersBlockState;
        this.rendersMirrored = rendersMirrored;
    }

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

    public static final List<Item> SMALL_BLOCK_ITEMS = List.of(
            Items.HEAVY_CORE,
            Items.FLOWER_POT,
            Items.LANTERN,
            Items.SOUL_LANTERN,
            Items.LEVER
    );

    public static final List<TagKey<Item>> SMALL_BLOCK_ITEM_TAGS = List.of(
            ItemTags.SKULLS,
            ItemTags.BUTTONS
    );

    public static final List<Item> BLOCK_2D_OVERRIDE_ITEMS = List.of(
            Items.IRON_BARS,
            Items.CHAIN,
            Items.GLASS_PANE,
            Items.WHITE_STAINED_GLASS_PANE,
            Items.ORANGE_STAINED_GLASS_PANE,
            Items.MAGENTA_STAINED_GLASS_PANE,
            Items.LIGHT_BLUE_STAINED_GLASS_PANE,
            Items.YELLOW_STAINED_GLASS_PANE,
            Items.LIME_STAINED_GLASS_PANE,
            Items.PINK_STAINED_GLASS_PANE,
            Items.GRAY_STAINED_GLASS_PANE,
            Items.LIGHT_GRAY_STAINED_GLASS_PANE,
            Items.CYAN_STAINED_GLASS_PANE,
            Items.PURPLE_STAINED_GLASS_PANE,
            Items.BLUE_STAINED_GLASS_PANE,
            Items.BROWN_STAINED_GLASS_PANE,
            Items.GREEN_STAINED_GLASS_PANE,
            Items.RED_STAINED_GLASS_PANE,
            Items.BLACK_STAINED_GLASS_PANE,
            Items.POINTED_DRIPSTONE,
            Items.SMALL_AMETHYST_BUD,
            Items.MEDIUM_AMETHYST_BUD,
            Items.LARGE_AMETHYST_BUD,
            Items.AMETHYST_CLUSTER,
            Items.RED_MUSHROOM,
            Items.BROWN_MUSHROOM,
            Items.DEAD_BUSH,
            Items.SHORT_GRASS,
            Items.TALL_GRASS,
            Items.FERN,
            Items.LARGE_FERN,
            Items.CRIMSON_FUNGUS,
            Items.WARPED_FUNGUS,
            Items.BAMBOO,
            Items.SUGAR_CANE,
            Items.SMALL_DRIPLEAF,
            Items.BIG_DRIPLEAF,
            Items.CRIMSON_ROOTS,
            Items.WARPED_ROOTS,
            Items.NETHER_SPROUTS,
            Items.WEEPING_VINES,
            Items.TWISTING_VINES,
            Items.VINE,
            Items.GLOW_BERRIES,
            Items.COCOA_BEANS,
            Items.LILY_PAD,
            Items.BRAIN_CORAL,
            Items.BUBBLE_CORAL,
            Items.HORN_CORAL,
            Items.FIRE_CORAL,
            Items.TUBE_CORAL,
            Items.DEAD_BRAIN_CORAL,
            Items.DEAD_BUBBLE_CORAL,
            Items.DEAD_HORN_CORAL,
            Items.DEAD_FIRE_CORAL,
            Items.DEAD_TUBE_CORAL,
            Items.BRAIN_CORAL_FAN,
            Items.BUBBLE_CORAL_FAN,
            Items.HORN_CORAL_FAN,
            Items.FIRE_CORAL_FAN,
            Items.TUBE_CORAL_FAN,
            Items.DEAD_BRAIN_CORAL_FAN,
            Items.DEAD_BUBBLE_CORAL_FAN,
            Items.DEAD_HORN_CORAL_FAN,
            Items.DEAD_FIRE_CORAL_FAN,
            Items.DEAD_TUBE_CORAL_FAN,
            Items.COBWEB,
            Items.LILAC,
            Items.PEONY,
            Items.ROSE_BUSH,
            Items.SUNFLOWER,
            Items.MANGROVE_PROPAGULE,
            Items.PINK_PETALS,
            Items.PITCHER_PLANT,
            Items.MELON_SEEDS,
            Items.PUMPKIN_SEEDS,
            Items.GLOW_LICHEN,
            Items.SCULK_VEIN,
            Items.NETHER_WART,
            Items.SWEET_BERRIES,
            Items.SEAGRASS,
            Items.KELP,
            Items.TORCH,
            Items.SOUL_TORCH,
            Items.REDSTONE_TORCH,
            Items.BELL,
            Items.LADDER,
            Items.LIGHTNING_ROD,
            Items.DECORATED_POT,
            Items.REDSTONE,
            Items.STRING,
            Items.TRIPWIRE_HOOK,
            Items.RAIL,
            Items.ACTIVATOR_RAIL,
            Items.DETECTOR_RAIL,
            Items.POWERED_RAIL,
            Items.FROGSPAWN,
            //? >= 1.21.4 {
            Items.PALE_HANGING_MOSS,
            Items.RESIN_CLUMP,
            //}
            //? >= 1.21.5 {
            Items.DRY_SHORT_GRASS,
            Items.DRY_TALL_GRASS,
            Items.BUSH,
            Items.FIREFLY_BUSH,
            Items.LEAF_LITTER,
            Items.CACTUS_FLOWER,
            Items.WILDFLOWERS
            //}
    );

    public static final List<TagKey<Item>> BLOCK_2D_OVERRIDE_ITEM_TAGS = List.of(
            ItemTags.CANDLES,
            ItemTags.BANNERS,
            ItemTags.SMALL_FLOWERS,
            ItemTags.VILLAGER_PLANTABLE_SEEDS,
            ItemTags.SAPLINGS,
            ItemTags.SIGNS,
            ItemTags.HANGING_SIGNS
    );

    public static FirstPersonGenericItemPose fromItemStack(ItemStack itemStack) {
        if (itemStack.is(ItemTags.ARROWS)) {
            return ARROW;
        }
        if (itemStack.is(ItemTags.BANNERS)) {
            return BANNER;
        }
        if (itemStack.is(ItemTags.DOORS)) {
            return DOOR_BLOCK;
        }
        for (Item item : ROD_ITEMS) {
            if (itemStack.is(item)) {
                return ROD;
            }
        }
        if (itemStack.getItem() instanceof BlockItem) {
            for (Item item : SMALL_BLOCK_ITEMS) {
                if (itemStack.is(item)) {
                    return SMALL_BLOCK;
                }
            }
            for (TagKey<Item> tag : SMALL_BLOCK_ITEM_TAGS) {
                if (itemStack.is(tag)) {
                    return SMALL_BLOCK;
                }
            }
            for (Item item : BLOCK_2D_OVERRIDE_ITEMS) {
                if (itemStack.is(item)) {
                    return DEFAULT_2D_ITEM;
                }
            }
            for (TagKey<Item> tag : BLOCK_2D_OVERRIDE_ITEM_TAGS) {
                if (itemStack.is(tag)) {
                    return DEFAULT_2D_ITEM;
                }
            }
            return BLOCK;
        }
        return DEFAULT_2D_ITEM;
    }

    public boolean shouldMirrorItemModel(FirstPersonHandPose handPose, HumanoidArm side) {
        if (side == HumanoidArm.RIGHT) {
            return false;
        }
        if (handPose != FirstPersonHandPose.GENERIC_ITEM) {
            return false;
        }
        return this.rendersMirrored;
    }

    public static PoseFunction<LocalSpacePose> constructPoseFunction(CachedPoseContainer cachedPoseContainer, InteractionHand interactionHand) {
        PoseFunction<LocalSpacePose> miningStateMachine = switch (interactionHand) {
            case MAIN_HAND -> ApplyAdditiveFunction.of(SequenceEvaluatorFunction.builder(context -> context.driverContainer().getDriverValue(FirstPersonDrivers.getGenericItemPoseDriver(interactionHand), 1).basePoseLocation).build(), MakeDynamicAdditiveFunction.of(
                    FirstPersonMining.constructPoseFunction(
                            cachedPoseContainer,
                            SequenceEvaluatorFunction.builder(FirstPersonAnimationSequences.HAND_EMPTY_POSE).build(),
                            SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_EMPTY_MINE_SWING)
                                    .looping(true)
                                    .setResetStartTimeOffset(TimeSpan.of60FramesPerSecond(20))
                                    .setPlayRate(evaluationState -> 1.35f * LocomotionMain.CONFIG.data().firstPersonPlayer.miningAnimationSpeedMultiplier)
                                    .build(),
                            SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_EMPTY_MINE_FINISH).build(),
                            Transition.builder(TimeSpan.of60FramesPerSecond(6)).setEasement(Easing.SINE_OUT).build()),
                    SequenceEvaluatorFunction.builder(FirstPersonAnimationSequences.HAND_EMPTY_POSE).build()));
            case OFF_HAND -> SequenceEvaluatorFunction.builder(context -> context.driverContainer().getDriverValue(FirstPersonDrivers.getGenericItemPoseDriver(interactionHand), 1).basePoseLocation).build();
        };

        return miningStateMachine;
    }
}